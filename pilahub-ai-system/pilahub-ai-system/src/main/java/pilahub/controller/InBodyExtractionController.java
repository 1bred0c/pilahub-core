package pilahub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pilahub.dto.request.InBodyBase64ExtractionRequest;
import pilahub.dto.response.InBodyExtractionResponse;
import pilahub.service.InBodyExtractionService;

@RestController
@RequestMapping("/api/v1/inbody")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "InBody Extraction", description = "Extract health metrics from InBody scan image using Gemini")
public class InBodyExtractionController {

    private final InBodyExtractionService inBodyExtractionService;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Extract InBody metrics from multipart image")
    public ResponseEntity<InBodyExtractionResponse> extractFromMultipart(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "rawScanId", required = false) String rawScanId) {

        log.info("Received InBody extraction request (multipart), filename: {}", image.getOriginalFilename());
        InBodyExtractionResponse response = inBodyExtractionService.extractFromMultipart(image, rawScanId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/extract", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Extract InBody metrics from base64 image")
    public ResponseEntity<InBodyExtractionResponse> extractFromBase64(
            @Valid @RequestBody InBodyBase64ExtractionRequest request) {

        log.info("Received InBody extraction request (base64)");
        InBodyExtractionResponse response = inBodyExtractionService.extractFromBase64(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("InBody Extraction Service is running");
    }
}

