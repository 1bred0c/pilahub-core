package pilahub.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pilahub.config.GeminiConfig;
import pilahub.dto.request.InBodyBase64ExtractionRequest;
import pilahub.dto.response.InBodyExtractionDataResponse;
import pilahub.dto.response.InBodyExtractionResponse;
import pilahub.service.InBodyExtractionService;

import java.math.BigDecimal;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class InBodyExtractionServiceImpl implements InBodyExtractionService {

    private static final String SOURCE_INBODY_SCAN = "INBODY_SCAN";

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;

    @Override
    public InBodyExtractionResponse extractFromMultipart(MultipartFile imageFile, String rawScanId) {
        try {
            if (imageFile == null || imageFile.isEmpty()) {
                throw new IllegalArgumentException("image file is required");
            }

            String mimeType = normalizeMimeType(imageFile.getContentType());
            validateMimeType(mimeType);

            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            log.info("Extracting InBody metrics from multipart image: {}", imageFile.getOriginalFilename());

            return extractFromGemini(base64Image, mimeType, rawScanId);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Failed to extract InBody metrics from multipart image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process multipart image", e);
        }
    }

    @Override
    public InBodyExtractionResponse extractFromBase64(InBodyBase64ExtractionRequest request) {
        try {
            String sanitizedBase64 = sanitizeBase64(request.getBase64Image());
            String mimeType = normalizeMimeType(request.getMimeType());
            validateMimeType(mimeType);

            // Validate base64 before calling Gemini
            Base64.getDecoder().decode(sanitizedBase64);

            log.info("Extracting InBody metrics from base64 image payload");
            return extractFromGemini(sanitizedBase64, mimeType, request.getRawScanId());
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Failed to extract InBody metrics from base64 image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process base64 image", e);
        }
    }

    private InBodyExtractionResponse extractFromGemini(String base64Image, String mimeType, String rawScanId) {
        try {
            String responseText = callGeminiForInBodyExtraction(base64Image, mimeType);
            JsonNode extractedNode = objectMapper.readTree(extractJsonFromResponse(responseText));

            InBodyExtractionDataResponse data = InBodyExtractionDataResponse.builder()
                    .heightCm(readDecimal(extractedNode, "heightCm"))
                    .weightKg(readDecimal(extractedNode, "weightKg"))
                    .bmi(readDecimal(extractedNode, "bmi"))
                    .bodyFatPercentage(readDecimal(extractedNode, "bodyFatPercentage"))
                    .muscleMassKg(readDecimal(extractedNode, "muscleMassKg"))
                    .waistCm(null)
                    .hipCm(null)
                    .source(SOURCE_INBODY_SCAN)
                    .metadata(buildMetadataJson(extractedNode.get("metadata"), rawScanId))
                    .build();

            String deviceName = readMetadataDeviceName(data.getMetadata());
            String message = StringUtils.hasText(deviceName)
                    ? "Extracted successfully from " + deviceName
                    : "Extracted successfully from InBody scan";

            return InBodyExtractionResponse.builder()
                    .status("success")
                    .data(data)
                    .message(message)
                    .build();
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error while parsing InBody extraction response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract structured data from Gemini response", e);
        }
    }

    private String callGeminiForInBodyExtraction(String base64Image, String mimeType) {
        if (!StringUtils.hasText(geminiConfig.getApiKey())) {
            throw new IllegalArgumentException("Gemini API key is not configured");
        }

        try {
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    geminiConfig.getModel(),
                    geminiConfig.getApiKey()
            );

            ObjectNode root = objectMapper.createObjectNode();
            ArrayNode contents = root.putArray("contents");
            ObjectNode contentItem = contents.addObject();
            ArrayNode parts = contentItem.putArray("parts");

            parts.addObject().put("text", buildInBodyPrompt());
            parts.addObject()
                    .putObject("inline_data")
                    .put("mime_type", mimeType)
                    .put("data", base64Image);

            root.putObject("generationConfig")
                    .put("response_mime_type", "application/json");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(root), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Gemini API returned empty or non-success response");
            }

            JsonNode responseNode = objectMapper.readTree(response.getBody());
            JsonNode textNode = responseNode.at("/candidates/0/content/parts/0/text");
            if (textNode.isMissingNode() || !textNode.isTextual()) {
                throw new RuntimeException("Unable to read text response from Gemini");
            }

            return textNode.asText();
        } catch (Exception e) {
            log.error("Gemini API call failed for InBody extraction: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Gemini API for image extraction", e);
        }
    }

    private String buildInBodyPrompt() {
        return "You are extracting health metrics from a single InBody scan image. " +
                "Return ONLY raw JSON and nothing else. " +
                "If a value is missing or unreadable, set it to null. " +
                "waistCm and hipCm MUST always be null because InBody sheet does not provide these fields. " +
                "Use this exact schema: " +
                "{\n" +
                "  \"heightCm\": number|null,\n" +
                "  \"weightKg\": number|null,\n" +
                "  \"bmi\": number|null,\n" +
                "  \"bodyFatPercentage\": number|null,\n" +
                "  \"muscleMassKg\": number|null,\n" +
                "  \"waistCm\": null,\n" +
                "  \"hipCm\": null,\n" +
                "  \"source\": \"INBODY_SCAN\",\n" +
                "  \"metadata\": {\n" +
                "    \"device\": string|null,\n" +
                "    \"test_date\": string|null,\n" +
                "    \"inbody_score\": number|null,\n" +
                "    \"visceral_fat_level\": number|null\n" +
                "  }\n" +
                "}";
    }

    private void validateMimeType(String mimeType) {
        if (!mimeType.equals("image/jpeg") && !mimeType.equals("image/png") && !mimeType.equals("image/webp")) {
            throw new IllegalArgumentException("Only image/jpeg, image/png, and image/webp are supported");
        }
    }

    private String normalizeMimeType(String mimeType) {
        return StringUtils.hasText(mimeType) ? mimeType.trim().toLowerCase() : "image/jpeg";
    }

    private String sanitizeBase64(String base64Image) {
        if (!StringUtils.hasText(base64Image)) {
            throw new IllegalArgumentException("base64Image is required");
        }

        String sanitized = base64Image.trim();
        int prefixIndex = sanitized.indexOf(",");
        if (sanitized.startsWith("data:") && prefixIndex > 0) {
            sanitized = sanitized.substring(prefixIndex + 1);
        }
        return sanitized;
    }

    private String extractJsonFromResponse(String rawResponse) {
        String cleaned = rawResponse == null ? "" : rawResponse.trim();
        cleaned = cleaned.replaceAll("^```json\\s*", "");
        cleaned = cleaned.replaceAll("^```\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```$", "");

        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        if (firstBrace == -1 || lastBrace == -1 || firstBrace > lastBrace) {
            throw new RuntimeException("No valid JSON found in Gemini response");
        }

        return cleaned.substring(firstBrace, lastBrace + 1);
    }

    private BigDecimal readDecimal(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }

        if (field.isNumber()) {
            return field.decimalValue();
        }

        if (field.isTextual() && StringUtils.hasText(field.asText())) {
            try {
                return new BigDecimal(field.asText().trim());
            } catch (NumberFormatException ignored) {
                log.warn("Cannot parse decimal for field {} from value {}", fieldName, field.asText());
            }
        }

        return null;
    }

    private String buildMetadataJson(JsonNode metadataNode, String rawScanId) {
        try {
            ObjectNode normalized = objectMapper.createObjectNode();
            if (metadataNode != null && !metadataNode.isNull()) {
                if (metadataNode.isObject()) {
                    normalized.setAll((ObjectNode) metadataNode);
                } else if (metadataNode.isTextual()) {
                    String metadataText = metadataNode.asText();
                    if (StringUtils.hasText(metadataText)) {
                        JsonNode parsed = objectMapper.readTree(metadataText);
                        if (parsed.isObject()) {
                            normalized.setAll((ObjectNode) parsed);
                        }
                    }
                }
            }

            if (StringUtils.hasText(rawScanId)) {
                normalized.put("raw_scan_id", rawScanId);
            }

            if (!normalized.has("device")) {
                normalized.putNull("device");
            }
            if (!normalized.has("test_date")) {
                normalized.putNull("test_date");
            }
            if (!normalized.has("inbody_score")) {
                normalized.putNull("inbody_score");
            }
            if (!normalized.has("visceral_fat_level")) {
                normalized.putNull("visceral_fat_level");
            }

            return objectMapper.writeValueAsString(normalized);
        } catch (Exception e) {
            log.warn("Failed to normalize metadata, falling back to default metadata object");
            ObjectNode fallback = objectMapper.createObjectNode();
            fallback.putNull("device");
            fallback.putNull("test_date");
            fallback.putNull("inbody_score");
            fallback.putNull("visceral_fat_level");
            if (StringUtils.hasText(rawScanId)) {
                fallback.put("raw_scan_id", rawScanId);
            }
            try {
                return objectMapper.writeValueAsString(fallback);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to serialize metadata", ex);
            }
        }
    }

    private String readMetadataDeviceName(String metadataJson) {
        try {
            JsonNode metadataNode = objectMapper.readTree(metadataJson);
            JsonNode deviceNode = metadataNode.get("device");
            if (deviceNode != null && deviceNode.isTextual() && StringUtils.hasText(deviceNode.asText())) {
                return deviceNode.asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

