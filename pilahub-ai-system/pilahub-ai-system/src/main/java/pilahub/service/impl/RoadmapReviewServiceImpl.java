package pilahub.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pilahub.config.GeminiConfig;
import pilahub.dto.request.RoadmapReviewAIRequest;
import pilahub.dto.response.RoadmapReviewAIResponse;
import pilahub.service.GeminiFileStoreService;
import pilahub.service.RoadmapReviewService;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoadmapReviewServiceImpl implements RoadmapReviewService {

    private final Client geminiClient;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;
    private final GeminiFileStoreService fileStoreService;

    @Override
    public RoadmapReviewAIResponse reviewRoadmap(RoadmapReviewAIRequest request) {
        try {
            log.info("Starting roadmap review for roadmapId: {}", request.getRoadmap().getRoadmapId());

            String documentUri = null;
            try {
                documentUri = fileStoreService.getActiveRoadmapReviewDocumentUri();
                if (documentUri != null) {
                    log.info("Using roadmap review reference document from File Store: {}", documentUri);
                } else {
                    log.info("No active roadmap review reference document found, proceeding without document");
                }
            } catch (Exception e) {
                log.warn("Error getting roadmap review reference document, proceeding without: {}", e.getMessage());
            }

            String prompt = buildRoadmapReviewPrompt(request, documentUri);
            log.debug("Roadmap review prompt built, length: {} characters", prompt.length());

            String rawResponse = callGeminiAPI(prompt);
            RoadmapReviewAIResponse response = parseRoadmapReviewResponse(rawResponse);

            log.info("Roadmap review completed. Overall score: {}, Confidence: {}",
                    response.getOverallScore(), response.getConfidenceLevel());

            return response;

        } catch (Exception e) {
            log.error("Error during roadmap review: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to review roadmap: " + e.getMessage(), e);
        }
    }

    @Override
    public String buildRoadmapReviewPrompt(RoadmapReviewAIRequest request, String documentUri) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Bạn là chuyên gia phân tích hiệu quả lộ trình tập luyện. ");
        prompt.append("Hãy đánh giá roadmap dựa trên dữ liệu đầu vào, và trả về kết quả dưới dạng JSON theo đúng schema. ");
        prompt.append("Tất cả nội dung phải bằng tiếng Việt.\n\n");

        if (documentUri != null) {
            prompt.append("=== TÀI LIỆU THAM KHẢO ===\n");
            prompt.append("📎 File quy định: ").append(documentUri).append("\n");
            prompt.append("Áp dụng đúng tiêu chí trong tài liệu tham khảo này khi chấm điểm và đưa khuyến nghị.\n\n");
        }

        var roadmap = request.getRoadmap();
        prompt.append("=== ROADMAP ===\n");
        prompt.append("ID: ").append(roadmap.getRoadmapId()).append("\n");
        prompt.append("Tiêu đề: ").append(roadmap.getTitle()).append("\n");
        prompt.append("Mô tả: ").append(roadmap.getDescription()).append("\n");
        prompt.append("Start: ").append(roadmap.getStartDate()).append("\n");
        prompt.append("End: ").append(roadmap.getEndDate()).append("\n");
        prompt.append("Tiến độ: ").append(roadmap.getProgressPercent()).append("%\n");
        prompt.append("Trạng thái: ").append(roadmap.getStatus()).append("\n");
        prompt.append("Nguồn: ").append(roadmap.getSource()).append("\n");
        if (roadmap.getGoals() != null && !roadmap.getGoals().isEmpty()) {
            prompt.append("Mục tiêu:\n");
            for (var goal : roadmap.getGoals()) {
                prompt.append("- ").append(goal.getName())
                        .append(" (code: ").append(goal.getCode())
                        .append(", primary: ").append(goal.getIsPrimary())
                        .append(", order: ").append(goal.getGoalOrder())
                        .append(")\n");
            }
        }
        prompt.append("\n");

        var initial = request.getInitialHealthProfile();
        var latest = request.getFinalHealthProfile();
        prompt.append("=== HEALTH PROFILE (INITIAL) ===\n");
        appendHealthProfile(prompt, initial);
        prompt.append("\n");

        prompt.append("=== HEALTH PROFILE (FINAL) ===\n");
        appendHealthProfile(prompt, latest);
        prompt.append("\n");

        var context = request.getTraineeContext();
        prompt.append("=== TRAINEE CONTEXT ===\n");
        prompt.append("Tuổi: ").append(context.getAge()).append("\n");
        prompt.append("Giới tính: ").append(context.getGender()).append("\n");
        prompt.append("Tần suất tập/tuần: ").append(context.getWorkoutFrequency()).append("\n\n");

        var execution = request.getExecutionSummary();
        prompt.append("=== EXECUTION SUMMARY ===\n");
        prompt.append("Tổng lịch: ").append(execution.getTotalSchedules()).append("\n");
        prompt.append("Hoàn thành lịch: ").append(execution.getCompletedSchedules()).append("\n");
        prompt.append("Tổng bài tập: ").append(execution.getTotalExercises()).append("\n");
        prompt.append("Hoàn thành bài tập: ").append(execution.getCompletedExercises()).append("\n");
        prompt.append("Completion rate: ").append(execution.getCompletionRate()).append("\n\n");

        prompt.append("=== DELTA METRICS (SERVER-CALCULATED) ===\n");
        var deltaMetrics = computeDeltaMetrics(request);
        appendDelta(prompt, "weightKg", deltaMetrics.getWeightKg());
        appendDelta(prompt, "bodyFat%", deltaMetrics.getBodyFatPercent());
        appendDelta(prompt, "muscleMassKg", deltaMetrics.getMuscleMassKg());
        appendDelta(prompt, "waistCm", deltaMetrics.getWaistCm());
        prompt.append("\n");

        prompt.append("=== YÊU CẦU CHẤM ĐIỂM ===\n");
        prompt.append("- overallScore và các subScores: thang 0-100 (số nguyên).\n");
        prompt.append("- safetyRisk: 100 nghĩa là rủi ro thấp, 0 nghĩa là rủi ro cao.\n");
        prompt.append("- adherence dựa vào completionRate và completedSchedules/totalSchedules.\n");
        prompt.append("- effectiveness/goalAchievement dựa trên mục tiêu và thay đổi chỉ số cơ thể.\n");
        prompt.append("- bodyCompositionChange tổng hợp thay đổi cân nặng + body fat + muscle mass + vòng eo.\n");
        prompt.append("- Nếu dữ liệu thiếu, nêu rõ trong narrativeSummary và điều chỉnh confidenceLevel.\n\n");

        prompt.append("=== ĐỊNH DẠNG JSON BẮT BUỘC ===\n");
        prompt.append("Return EXACTLY this JSON format and nothing else:\n\n");
        prompt.append("{\n");
        prompt.append("  \"overallScore\": 0,\n");
        prompt.append("  \"subScores\": {\n");
        prompt.append("    \"effectiveness\": 0,\n");
        prompt.append("    \"adherence\": 0,\n");
        prompt.append("    \"bodyCompositionChange\": 0,\n");
        prompt.append("    \"muscleChange\": 0,\n");
        prompt.append("    \"waistChange\": 0,\n");
        prompt.append("    \"goalAchievement\": 0,\n");
        prompt.append("    \"safetyRisk\": 0\n");
        prompt.append("  },\n");
        prompt.append("  \"deltaMetrics\": {\n");
        prompt.append("    \"weightKg\": {\"baseline\": 0, \"final\": 0, \"percent\": 0},\n");
        prompt.append("    \"bodyFat%\": {\"baseline\": 0, \"final\": 0, \"percent\": 0},\n");
        prompt.append("    \"muscleMassKg\": {\"baseline\": 0, \"final\": 0, \"percent\": 0},\n");
        prompt.append("    \"waistCm\": {\"baseline\": 0, \"final\": 0, \"percent\": 0}\n");
        prompt.append("  },\n");
        prompt.append("  \"narrativeSummary\": \"...\",\n");
        prompt.append("  \"prioritizedRecommendations\": [\n");
        prompt.append("    {\"recommendation\": \"...\", \"rationale\": \"...\"}\n");
        prompt.append("  ],\n");
        prompt.append("  \"confidenceLevel\": 0\n");
        prompt.append("}\n\n");

        prompt.append("=== QUY TẮC BỔ SUNG ===\n");
        prompt.append("- percent trong deltaMetrics làm tròn 1 chữ số thập phân.\n");
        prompt.append("- Nếu baseline bị thiếu hoặc = 0, đặt percent là null.\n");
        prompt.append("- Ưu tiên khuyến nghị theo tác động (high impact trước).\n");
        prompt.append("- Nếu dùng tài liệu tham khảo, chèn trích dẫn trong rationale (ví dụ: \"...【12†L10-L18】\").\n");
        prompt.append("- Trả về chỉ JSON, không markdown, không giải thích thêm.\n\n");

        prompt.append("BẮT ĐẦU ĐÁNH GIÁ ROADMAP:");

        return prompt.toString();
    }

    @Override
    public RoadmapReviewAIResponse parseRoadmapReviewResponse(String rawResponse) {
        try {
            String jsonString = extractJsonFromResponse(rawResponse);
            return objectMapper.readValue(jsonString, RoadmapReviewAIResponse.class);
        } catch (Exception e) {
            log.error("Error parsing roadmap review response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse roadmap review response: " + e.getMessage(), e);
        }
    }

    private String callGeminiAPI(String prompt) {
        try {
            log.info("Calling Gemini API with model: {}", geminiConfig.getModel());

            GenerateContentResponse response = geminiClient.models.generateContent(
                    geminiConfig.getModel(),
                    prompt,
                    null
            );

            String responseText = response.text();
            log.info("Received response from Gemini API, length: {} characters", responseText.length());
            return responseText;

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private String extractJsonFromResponse(String rawResponse) {
        String cleaned = rawResponse == null ? "" : rawResponse.trim();

        cleaned = cleaned.replaceAll("^```json\\s*", "");
        cleaned = cleaned.replaceAll("^```\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```$", "");

        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        if (firstBrace == -1 || lastBrace == -1 || firstBrace >= lastBrace) {
            throw new RuntimeException("No valid JSON object found in response");
        }

        return cleaned.substring(firstBrace, lastBrace + 1);
    }

    private void appendHealthProfile(StringBuilder prompt, RoadmapReviewAIRequest.HealthProfileSnapshot profile) {
        prompt.append("ID: ").append(profile.getHealthProfileId()).append("\n");
        prompt.append("Created: ").append(profile.getCreatedAt()).append("\n");
        prompt.append("Height(cm): ").append(profile.getHeightCm()).append("\n");
        prompt.append("Weight(kg): ").append(profile.getWeightKg()).append("\n");
        prompt.append("BMI: ").append(profile.getBmi()).append("\n");
        prompt.append("Body fat(%): ").append(profile.getBodyFatPercentage()).append("\n");
        prompt.append("Muscle mass(kg): ").append(profile.getMuscleMassKg()).append("\n");
        prompt.append("Waist(cm): ").append(profile.getWaistCm()).append("\n");
        prompt.append("Hip(cm): ").append(profile.getHipCm()).append("\n");
        prompt.append("Source: ").append(profile.getSource()).append("\n");
        prompt.append("Metadata: ").append(profile.getMetadata()).append("\n");
    }

    private RoadmapReviewAIResponse.DeltaMetrics computeDeltaMetrics(RoadmapReviewAIRequest request) {
        var initial = request.getInitialHealthProfile();
        var latest = request.getFinalHealthProfile();

        return RoadmapReviewAIResponse.DeltaMetrics.builder()
                .weightKg(computeMetricDelta(initial.getWeightKg(), latest.getWeightKg()))
                .bodyFatPercent(computeMetricDelta(initial.getBodyFatPercentage(), latest.getBodyFatPercentage()))
                .muscleMassKg(computeMetricDelta(initial.getMuscleMassKg(), latest.getMuscleMassKg()))
                .waistCm(computeMetricDelta(initial.getWaistCm(), latest.getWaistCm()))
                .build();
    }

    private RoadmapReviewAIResponse.MetricDelta computeMetricDelta(BigDecimal baseline, BigDecimal finalValue) {
        if (baseline == null || finalValue == null) {
            return RoadmapReviewAIResponse.MetricDelta.builder()
                    .baseline(baseline)
                    .finalValue(finalValue)
                    .percent(null)
                    .build();
        }

        BigDecimal percent = null;
        if (baseline.compareTo(BigDecimal.ZERO) != 0) {
            percent = finalValue.subtract(baseline)
                    .divide(baseline, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(1, RoundingMode.HALF_UP);
        }

        return RoadmapReviewAIResponse.MetricDelta.builder()
                .baseline(baseline)
                .finalValue(finalValue)
                .percent(percent)
                .build();
    }

    private void appendDelta(StringBuilder prompt, String label, RoadmapReviewAIResponse.MetricDelta delta) {
        prompt.append(label)
                .append(": baseline=").append(delta.getBaseline())
                .append(", final=").append(delta.getFinalValue())
                .append(", percent=").append(delta.getPercent())
                .append("\n");
    }
}

