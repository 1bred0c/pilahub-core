package pilahub.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pilahub.config.GeminiConfig;
import pilahub.dto.request.WorkoutFeedbackAIRequest;
import pilahub.dto.request.WorkoutFeedbackAnalysisRequest;
import pilahub.dto.response.WorkoutFeedbackAnalysisResponse;
import pilahub.service.GeminiFileStoreService;
import pilahub.service.WorkoutFeedbackService;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutFeedbackServiceImpl implements WorkoutFeedbackService {

    private final Client geminiClient;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;
    private final GeminiFileStoreService fileStoreService;

    @Override
    public WorkoutFeedbackAnalysisResponse analyzeWorkoutFeedback(WorkoutFeedbackAnalysisRequest request) {
        try {
            log.info("Starting workout feedback analysis for exercise: {}", request.getExerciseName());

            // Step 1: Build detailed prompt in Vietnamese
            String prompt = buildWorkoutFeedbackPrompt(request);
            log.debug("Prompt built successfully");

            // Step 2: Call Gemini API
            String rawResponse = callGeminiAPI(prompt);
            log.debug("Received response from Gemini API");

            // Step 3: Parse response and extract structured feedback
            WorkoutFeedbackAnalysisResponse response = parseWorkoutFeedback(rawResponse, request);

            log.info("Workout feedback analysis completed successfully. Form Score: {}, Overall Score: {}",
                    response.getFormScore(), response.getOverallScore());

            return response;

        } catch (Exception e) {
            log.error("Error during workout feedback analysis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze workout feedback: " + e.getMessage(), e);
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

    private String buildWorkoutFeedbackPrompt(WorkoutFeedbackAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Bạn là một chuyên gia phân tích tập luyện Pilates và đánh giá kỹ thuật. ")
                .append("Hãy phân tích chi tiết feedback cho buổi tập luyện sau và cung cấp đánh giá toàn diện bằng tiếng Việt.\n\n");

        // Exercise Information
        prompt.append("=== THÔNG TIN BÀI TẬP ===\n");
        prompt.append("Tên bài tập: ").append(request.getExerciseName()).append("\n");
        prompt.append("Loại bài tập: ").append(request.getExerciseType()).append("\n");
        prompt.append("Độ khó: ").append(request.getDifficultyLevel()).append("\n");
        prompt.append("Bộ phận cơ thể nhắm tới: ").append(String.join(", ", request.getTargetBodyParts())).append("\n");
        prompt.append("Thời lượng: ").append(String.format("%.1f", request.getDurationSeconds())).append(" giây\n\n");

        // Mistake Information
        prompt.append("=== THÔNG TIN LỖI KỸ THUẬT ===\n");
        prompt.append("Tổng số lỗi: ").append(request.getMistakeSummary().getTotalMistakes()).append("\n");
        if (request.getMistakeSummary().getAverageTimeBetweenMistakes() != null) {
            prompt.append("Thời gian trung bình giữa các lỗi: ").append(String.format("%.1f", request.getMistakeSummary().getAverageTimeBetweenMistakes())).append(" giây\n");
        }
        prompt.append("\nChi tiết lỗi theo bộ phận cơ thể:\n");
        for (var mistake : request.getMistakeSummary().getMistakesByBodyPart()) {
            prompt.append("- ").append(mistake.getBodyPartName()).append(" (").append(mistake.getCount()).append(" lỗi):\n");
            for (var detail : mistake.getDetails()) {
                prompt.append("  • ").append(detail).append("\n");
            }
        }
        prompt.append("\n");

        // Heart Rate Information (if available)
        if (request.getHeartRateSummary() != null) {
            prompt.append("=== THÔNG TIN NHỊP TIM ===\n");
            prompt.append("Nhịp tim trung bình: ").append(request.getHeartRateSummary().getAverageHeartRate()).append(" bpm\n");
            prompt.append("Nhịp tim tối đa: ").append(request.getHeartRateSummary().getMaxHeartRate()).append(" bpm\n");
            prompt.append("Nhịp tim tối thiểu: ").append(request.getHeartRateSummary().getMinHeartRate()).append(" bpm\n");
            prompt.append("Phân bố vùng nhịp tim:\n");
            prompt.append("- Vùng nghỉ (< 100 bpm): ").append(String.format("%.1f", request.getHeartRateSummary().getZones().getRestZone())).append("%\n");
            prompt.append("- Vùng đốt mỡ (100-129 bpm): ").append(String.format("%.1f", request.getHeartRateSummary().getZones().getFatBurnZone())).append("%\n");
            prompt.append("- Vùng cardio (130-159 bpm): ").append(String.format("%.1f", request.getHeartRateSummary().getZones().getCardioZone())).append("%\n");
            prompt.append("- Vùng đỉnh (≥ 160 bpm): ").append(String.format("%.1f", request.getHeartRateSummary().getZones().getPeakZone())).append("%\n\n");
        } else {
            prompt.append("=== THÔNG TIN NHỊP TIM ===\nKhông có dữ liệu nhịp tim\n\n");
        }

        // User Context
        prompt.append("=== THÔNG TIN NGƯỜI DÙNG ===\n");
        prompt.append("Tuổi: ").append(request.getUserContext().getAge()).append("\n");
        prompt.append("Giới tính: ").append(request.getUserContext().getGender()).append("\n");
        prompt.append("Mức độ tập luyện: ").append(request.getUserContext().getWorkoutLevel()).append("\n");
        prompt.append("Tần suất tập luyện: ").append(request.getUserContext().getWorkoutFrequency()).append("\n");
        if (request.getUserContext().getBmi() != null) {
            prompt.append("BMI: ").append(String.format("%.1f", request.getUserContext().getBmi())).append("\n");
        }
        if (request.getUserContext().getActiveInjuries() != null && !request.getUserContext().getActiveInjuries().isEmpty()) {
            prompt.append("Chấn thương/Vấn đề sức khỏe hiện tại: ").append(String.join(", ", request.getUserContext().getActiveInjuries())).append("\n");
        } else {
            prompt.append("Chấn thương/Vấn đề sức khỏe hiện tại: Không có\n");
        }
        prompt.append("\n");

        // Analysis Instructions
        prompt.append("=== HƯỚNG DẪN PHÂN TÍCH ===\n");
        prompt.append("Dựa trên thông tin trên, vui lòng cung cấp phân tích chi tiết và có giải thích về:\n\n");

        prompt.append("1. **ĐIỂM KỸ THUẬT (0-100)**: Đánh giá chất lượng thực hiện bài tập dựa trên:\n");
        prompt.append("   - Tần suất và mức độ nghiêm trọng của các lỗi\n");
        prompt.append("   - Phân bố lỗi theo thời gian (tập trung ở đầu/giữa/cuối hay phân tán đều)\n");
        prompt.append("   - Mức độ kinh nghiệm của người tập (dễ dàng hơn với người mới bắt đầu)\n");
        prompt.append("   - Cải thiện hay giảm chất lượng qua thời gian\n");
        prompt.append("   Giải thích chi tiết: TẠI SAO bạn đưa ra điểm này? Dựa vào những yếu tố nào?\n\n");

        if (request.getHeartRateSummary() != null) {
            prompt.append("2. **ĐIỂM SỨC BỀN (0-100)**: Đánh giá khả năng tim mạch dựa trên:\n");
            prompt.append("   - Vùng nhịp tim phù hợp với cường độ bài tập\n");
            prompt.append("   - Hiệu quả tim mạch (khả năng ổn định nhịp tim)\n");
            prompt.append("   - Phù hợp với độ tuổi và mức độ tập luyện\n");
            prompt.append("   Giải thích chi tiết: TẠI SAO bạn đưa ra điểm này? Phân tích vùng nhịp tim như thế nào?\n\n");

            prompt.append("3. **ĐIỂM TỔNG HỢP (0-100)**: Kết hợp điểm kỹ thuật (60%) và sức bền (40%)\n");
        } else {
            prompt.append("2. **ĐIỂM TỔNG HỢP (0-100)**: Bằng điểm kỹ thuật (không có dữ liệu nhịp tim)\n");
        }
        prompt.append("   Giải thích chi tiết: Cách tính như thế nào?\n\n");

        prompt.append("4. **ĐIỂM MẠNH** (2000 ký tự tối đa): Những khía cạnh tích cực của buổi tập\n");
        prompt.append("   - Những mẫu tích cực cụ thể từ dữ liệu\n");
        prompt.append("   - Tham chiếu đến thời điểm cụ thể (ví dụ: \"ở giây 45-90\")\n");
        prompt.append("   - Những cải tiến hoặc sự nhất quán\n");
        prompt.append("   - Lời khuyến khích nhưng trung thực\n");
        prompt.append("   Giải thích: TẠI SAO đây là điểm mạnh? Chứng cứ cụ thể là gì?\n\n");

        prompt.append("5. **ĐIỂM YẾU** (2000 ký tự tối đa): Những lĩnh vực cần cải thiện\n");
        prompt.append("   - Những vấn đề cụ thể từ nhật ký lỗi\n");
        prompt.append("   - Các mẫu (ví dụ: lỗi tập trung khi mệt hay ở cuối bài)\n");
        prompt.append("   - Bộ phận cơ thể có vấn đề lặp lại\n");
        prompt.append("   - Xây dựng, không làm nản lòng\n");
        prompt.append("   Giải thích: TẠI SAO đây là vấn đề? Dữ liệu nào cho thấy điều này?\n\n");

        prompt.append("6. **KHUYẾN NGHỊ** (2000 ký tự tối đa): Lời khuyên hành động cụ thể để cải thiện\n");
        prompt.append("   - Các bước cụ thể, có thể thực hành\n");
        prompt.append("   - Tùy chỉnh dựa trên:\n");
        prompt.append("     * Mức độ kinh nghiệm của người tập (beginner/intermediate/advanced)\n");
        prompt.append("     * Chấn thương hiện tại (RẤT QUAN TRỌNG!)\n");
        prompt.append("     * Tần suất tập luyện\n");
        prompt.append("     * Những lỗi cụ thể được quan sát\n");
        prompt.append("   - Gợi ý sửa đổi hoặc bài tập thay thế nếu thích hợp\n");
        prompt.append("   Giải thích: TẠI SAO những khuyến nghị này phù hợp với người dùng cụ thể này?\n\n");

        prompt.append("=== ĐỊNH DẠNG PHẢN HỒI ===\n");
        prompt.append("Vui lòng cung cấp phản hồi được cấu trúc với các phần sau, mỗi phần có giải thích chi tiết:\n\n");
        prompt.append("ĐIỂM KỸ THUẬT: [số] | Giải thích: [chi tiết]\n");
        if (request.getHeartRateSummary() != null) {
            prompt.append("ĐIỂM SỨC BỀN: [số] | Giải thích: [chi tiết]\n");
        }
        prompt.append("ĐIỂM TỔNG HỢP: [số] | Giải thích: [chi tiết]\n\n");
        prompt.append("ĐIỂM MẠNH: [chi tiết với giải thích]\n\n");
        prompt.append("ĐIỂM YẾU: [chi tiết với giải thích]\n\n");
        prompt.append("KHUYẾN NGHỊ: [chi tiết với giải thích]\n\n");

        return prompt.toString();
    }

    private WorkoutFeedbackAnalysisResponse parseWorkoutFeedback(String rawResponse, WorkoutFeedbackAnalysisRequest request) {
        try {
            log.info("Parsing workout feedback from AI response");

            // Extract scores from response
            Double formScore = extractScore(rawResponse, "ĐIỂM KỸ THUẬT", request.getMistakeSummary().getTotalMistakes());
            Double enduranceScore = null;
            if (request.getHeartRateSummary() != null) {
                enduranceScore = extractScore(rawResponse, "ĐIỂM SỨC BỀN", 0);
            }
            Double overallScore = extractScore(rawResponse, "ĐIỂM TỔNG HỢP", 0);

            // Extract narrative sections
            String strengths = extractSection(rawResponse, "ĐIỂM MẠNH");
            String weaknesses = extractSection(rawResponse, "ĐIỂM YẾU");
            String recommendations = extractSection(rawResponse, "KHUYẾN NGHỊ");

            WorkoutFeedbackAnalysisResponse response = WorkoutFeedbackAnalysisResponse.builder()
                    .totalMistakes(request.getMistakeSummary().getTotalMistakes())
                    .formScore(formScore)
                    .enduranceScore(enduranceScore)
                    .overallScore(overallScore)
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .recommendations(recommendations)
                    .aiModel(geminiConfig.getModel())
                    .analyzedAt(Instant.now())
                    .build();

            return response;

        } catch (Exception e) {
            log.error("Error parsing workout feedback: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse workout feedback: " + e.getMessage(), e);
        }
    }

    private Double extractScore(String response, String label, Integer defaultValue) {
        try {
            int index = response.indexOf(label);
            if (index == -1) {
                return defaultValue != null ? defaultValue.doubleValue() : 0.0;
            }

            // Find the number after the label
            String substring = response.substring(index, Math.min(index + 200, response.length()));
            String[] parts = substring.split("[^0-9.]+");

            for (String part : parts) {
                if (!part.isEmpty() && !part.equals(".")) {
                    try {
                        Double score = Double.parseDouble(part);
                        if (score >= 0 && score <= 100) {
                            return score;
                        }
                    } catch (NumberFormatException ignored) {
                        // Continue searching
                    }
                }
            }

            return defaultValue != null ? defaultValue.doubleValue() : 0.0;

        } catch (Exception e) {
            log.warn("Error extracting score for label: {}", label, e);
            return defaultValue != null ? defaultValue.doubleValue() : 0.0;
        }
    }

    private String extractSection(String response, String sectionLabel) {
        try {
            // Tìm section label (có thể có markdown formatting như **, ###, etc.)
            int startIndex = findSectionStart(response, sectionLabel);
            if (startIndex == -1) {
                log.warn("Section label '{}' not found in response", sectionLabel);
                return "";
            }

            // Move past the label và dấu :
            startIndex = response.indexOf(":", startIndex);
            if (startIndex == -1) {
                return "";
            }
            startIndex += 1;

            // Tìm section tiếp theo (với nhiều pattern khác nhau)
            String nextSectionLabel = nextSection(sectionLabel);
            int endIndex = -1;
            
            if (!nextSectionLabel.isEmpty()) {
                endIndex = findSectionStart(response, nextSectionLabel);
                
                // Nếu tìm thấy section tiếp theo, lùi lại để không bao gồm newline/markdown trước nó
                if (endIndex != -1) {
                    // Lùi lại để tìm newline cuối cùng trước section tiếp theo
                    while (endIndex > startIndex && 
                           (response.charAt(endIndex - 1) == '\n' || 
                            response.charAt(endIndex - 1) == '\r' ||
                            response.charAt(endIndex - 1) == '*' ||
                            response.charAt(endIndex - 1) == '#' ||
                            response.charAt(endIndex - 1) == ' ')) {
                        endIndex--;
                    }
                }
            }
            
            if (endIndex == -1) {
                endIndex = response.length();
            }

            String extracted = response.substring(startIndex, endIndex).trim();
            
            // Loại bỏ markdown artifacts ở đầu và cuối
            extracted = cleanMarkdownArtifacts(extracted);
            
            log.debug("Extracted section '{}': {} characters", sectionLabel, extracted.length());
            return extracted;

        } catch (Exception e) {
            log.error("Error extracting section '{}': {}", sectionLabel, e.getMessage(), e);
            return "";
        }
    }

    /**
     * Tìm vị trí bắt đầu của section label, bỏ qua markdown formatting
     */
    private int findSectionStart(String response, String sectionLabel) {
        // Thử tìm với các pattern khác nhau:
        // 1. Plain text: "ĐIỂM MẠNH"
        // 2. Markdown bold: "**ĐIỂM MẠNH**"
        // 3. Markdown heading: "### ĐIỂM MẠNH"
        // 4. Combined: "**ĐIỂM MẠNH:**"
        
        String[] patterns = {
            sectionLabel,                          // Plain
            "**" + sectionLabel + "**",           // Bold
            "**" + sectionLabel,                  // Bold start
            "###" + sectionLabel,                 // Heading
            "## " + sectionLabel,                 // Heading with space
            "# " + sectionLabel                   // Heading with space
        };
        
        for (String pattern : patterns) {
            int index = response.indexOf(pattern);
            if (index != -1) {
                return index;
            }
        }
        
        return -1;
    }

    /**
     * Loại bỏ các markdown artifacts ở đầu/cuối string
     */
    private String cleanMarkdownArtifacts(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Loại bỏ **, ###, whitespace ở đầu và cuối
        text = text.replaceAll("^[\\s*#]+", "").replaceAll("[\\s*#]+$", "");
        
        return text.trim();
    }

    private String nextSection(String currentSection) {
        return switch (currentSection) {
            case "ĐIỂM MẠNH" -> "ĐIỂM YẾU";
            case "ĐIỂM YẾU" -> "KHUYẾN NGHỊ";
            default -> "";
        };
    }

    @Override
    public WorkoutFeedbackAnalysisResponse analyzeWorkoutFeedbackAI(WorkoutFeedbackAIRequest request) {
        try {
            log.info("Starting AI workout feedback analysis for session: {}", request.getWorkoutSessionId());

            // Step 1: Try to get reference document URI from File Store
            String documentUri = null;
            try {
                documentUri = fileStoreService.getActiveWorkoutFeedbackDocumentUri();
                if (documentUri != null) {
                    log.info("Using workout feedback reference document from File Store: {}", documentUri);
                } else {
                    log.info("No active workout feedback reference document found, analyzing without document");
                }
            } catch (Exception e) {
                log.warn("Error getting workout feedback reference document, proceeding without: {}", e.getMessage());
            }

            // Step 2: Build comprehensive prompt from new input structure
            String prompt = buildWorkoutFeedbackAIPrompt(request, documentUri);
            log.debug("Prompt built successfully, length: {} characters", prompt.length());

            // Step 3: Call Gemini API
            String rawResponse = callGeminiAPI(prompt);
            log.debug("Received response from Gemini API");

            // Step 4: Parse response
            WorkoutFeedbackAnalysisResponse response = parseWorkoutFeedbackAI(rawResponse, request);

            log.info("AI workout feedback analysis completed. Form Score: {}, Overall Score: {}",
                    response.getFormScore(), response.getOverallScore());

            return response;

        } catch (Exception e) {
            log.error("Error during AI workout feedback analysis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze workout feedback: " + e.getMessage(), e);
        }
    }

    private String buildWorkoutFeedbackAIPrompt(WorkoutFeedbackAIRequest request, String documentUri) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Bạn là một chuyên gia phân tích tập luyện Pilates và đánh giá kỹ thuật. ");
        prompt.append("Hãy phân tích chi tiết feedback cho buổi tập luyện sau và cung cấp đánh giá toàn diện bằng tiếng Việt.\n\n");

        // Add reference document if available
        if (documentUri != null) {
            prompt.append("=== TÀI LIỆU THAM KHẢO ===\n");
            prompt.append("📎 File quy định: ").append(documentUri).append("\n");
            prompt.append("ÁP DỤNG CHÍNH XÁC các quy tắc và tiêu chí đánh giá từ tài liệu tham khảo này.\n\n");
        }

        // Trainee Information
        prompt.append("=== THÔNG TIN HỌC VIÊN ===\n");
        var trainee = request.getTraineeInfo();
        prompt.append("Tên: ").append(trainee.getName()).append("\n");
        if (trainee.getAge() != null) {
            prompt.append("Tuổi: ").append(trainee.getAge()).append("\n");
        }
        if (trainee.getFitnessLevel() != null) {
            prompt.append("Mức độ: ").append(trainee.getFitnessLevel()).append("\n");
        }
        if (trainee.getExperienceMonths() != null) {
            prompt.append("Kinh nghiệm: ").append(trainee.getExperienceMonths()).append(" tháng\n");
        }
        if (trainee.getGoals() != null && !trainee.getGoals().isEmpty()) {
            prompt.append("Mục tiêu: ").append(String.join(", ", trainee.getGoals())).append("\n");
        }
        if (trainee.getInjuries() != null && !trainee.getInjuries().isEmpty()) {
            prompt.append("Chấn thương/Lưu ý: ").append(String.join(", ", trainee.getInjuries())).append("\n");
        }
        prompt.append("\n");

        // Exercise Information
        prompt.append("=== THÔNG TIN BÀI TẬP ===\n");
        var exercise = request.getExerciseInfo();
        prompt.append("Tên: ").append(exercise.getName()).append("\n");
        if (exercise.getExerciseType() != null) {
            prompt.append("Loại: ").append(exercise.getExerciseType()).append("\n");
        }
        if (exercise.getDifficultyLevel() != null) {
            prompt.append("Độ khó: ").append(exercise.getDifficultyLevel()).append("\n");
        }
        if (exercise.getTargetBodyParts() != null && !exercise.getTargetBodyParts().isEmpty()) {
            prompt.append("Bộ phận nhắm tới: ").append(String.join(", ", exercise.getTargetBodyParts())).append("\n");
        }
        if (exercise.getCommonMistakes() != null) {
            prompt.append("Lỗi thường gặp: ").append(exercise.getCommonMistakes()).append("\n");
        }
        prompt.append("\n");

        // Session Metrics
        prompt.append("=== THÔNG SỐ BUỔI TẬP ===\n");
        var metrics = request.getSessionMetrics();
        prompt.append("Thời lượng: ").append(String.format("%.1f", metrics.getTotalDuration())).append(" giây\n");
        if (metrics.getCompletedReps() != null && metrics.getTargetReps() != null) {
            prompt.append("Số lần: ").append(metrics.getCompletedReps()).append("/").append(metrics.getTargetReps()).append("\n");
        }
        if (metrics.getAverageHeartRate() != null) {
            prompt.append("Nhịp tim TB: ").append(String.format("%.1f", metrics.getAverageHeartRate())).append(" bpm\n");
            if (metrics.getMaxHeartRate() != null) {
                prompt.append("Nhịp tim max: ").append(metrics.getMaxHeartRate()).append(" bpm\n");
            }
        }
        if (metrics.getCaloriesBurned() != null) {
            prompt.append("Calo đốt: ").append(String.format("%.1f", metrics.getCaloriesBurned())).append("\n");
        }
        prompt.append("Theo dõi AI: ").append(metrics.getHadAITracking() ? "Có" : "Không").append("\n");
        prompt.append("Theo dõi IoT: ").append(metrics.getHadIOTTracking() ? "Có" : "Không").append("\n");
        prompt.append("\n");

        // Mistake Summary - MOST IMPORTANT
        prompt.append("=== CHI TIẾT LỖI KỸ THUẬT (QUAN TRỌNG NHẤT) ===\n");
        var mistakes = request.getMistakeSummary();
        prompt.append("Tổng số lỗi: ").append(mistakes.getTotalMistakes()).append("\n");
        if (mistakes.getAverageTimeBetweenMistakes() != null) {
            prompt.append("TB thời gian giữa các lỗi: ").append(String.format("%.1f", mistakes.getAverageTimeBetweenMistakes())).append(" giây\n");
        }
        if (mistakes.getTotalMistakeDuration() != null) {
            prompt.append("Tổng thời gian lỗi: ").append(String.format("%.1f", mistakes.getTotalMistakeDuration())).append(" giây\n");
        }
        if (mistakes.getMistakeTimePercentage() != null) {
            prompt.append("Tỷ lệ thời gian lỗi: ").append(String.format("%.1f", mistakes.getMistakeTimePercentage())).append("%\n");
        }
        prompt.append("\n");

        // Detailed Mistakes Timeline
        prompt.append("CHI TIẾT TỪNG LỖI (theo timeline):\n");
        for (var detail : mistakes.getDetailedMistakes()) {
            prompt.append(String.format("- [%s] tại giây %.1f (kéo dài %.1fs): %s\n",
                    detail.getBodyPartName(),
                    detail.getRecordedAtSecond(),
                    detail.getDuration(),
                    detail.getDetails()));
        }
        prompt.append("\n");

        // Mistakes by Body Part Summary
        prompt.append("TỔNG KẾT THEO BỘ PHẬN:\n");
        for (var bodyPart : mistakes.getMistakesByBodyPart()) {
            prompt.append(String.format("- %s (%d lỗi", bodyPart.getBodyPartName(), bodyPart.getCount()));
            if (bodyPart.getTotalDuration() != null) {
                prompt.append(String.format(", %.1fs", bodyPart.getTotalDuration()));
            }
            prompt.append("):\n");
            for (String detail : bodyPart.getDetails()) {
                prompt.append("  • ").append(detail).append("\n");
            }
        }
        prompt.append("\n");

        // Analysis Instructions
        prompt.append("=== HƯỚNG DẪN PHÂN TÍCH ===\n");
        prompt.append("Dựa trên thông tin trên");
        if (documentUri != null) {
            prompt.append(" VÀ tài liệu tham khảo đính kèm");
        }
        prompt.append(", vui lòng cung cấp phân tích chi tiết:\n\n");

        prompt.append("1. **ĐIỂM KỸ THUẬT (0-100)**: Đánh giá chất lượng thực hiện\n");
        prompt.append("   - Dựa trên số lượng, mức độ nghiêm trọng, và thời gian lỗi\n");
        prompt.append("   - Xem xét mức độ kinh nghiệm của học viên\n");
        prompt.append("   - Phân tích pattern lỗi (đầu/giữa/cuối bài)\n\n");

        if (metrics.getAverageHeartRate() != null) {
            prompt.append("2. **ĐIỂM SỨC BỀN (0-100)**: Đánh giá khả năng tim mạch\n");
            prompt.append("   - Dựa trên nhịp tim trung bình và max\n");
            prompt.append("   - Phù hợp với độ tuổi và cường độ bài tập\n\n");
            prompt.append("3. **ĐIỂM TỔNG HỢP (0-100)**: Kỹ thuật (60%) + Sức bền (40%)\n\n");
        } else {
            prompt.append("2. **ĐIỂM TỔNG HỢP (0-100)**: Bằng điểm kỹ thuật (không có dữ liệu nhịp tim)\n\n");
        }

        prompt.append("4. **ĐIỂM MẠNH** (max 2000 ký tự): Những điểm tích cực\n");
        prompt.append("   - Tham chiếu cụ thể đến timeline và dữ liệu\n");
        prompt.append("   - Khuyến khích nhưng trung thực\n\n");

        prompt.append("5. **ĐIỂM YẾU** (max 2000 ký tự): Cần cải thiện\n");
        prompt.append("   - Chỉ ra vấn đề cụ thể từ dữ liệu lỗi\n");
        prompt.append("   - Phân tích pattern và nguyên nhân\n\n");

        prompt.append("6. **KHUYẾN NGHỊ** (max 2000 ký tự): Hướng dẫn cải thiện\n");
        prompt.append("   - Tùy chỉnh theo mức độ kinh nghiệm\n");
        prompt.append("   - CHÚ Ý chấn thương hiện tại (nếu có)\n");
        prompt.append("   - Gợi ý cụ thể, có thể thực hành\n\n");

        prompt.append("=== ĐỊNH DẠNG PHẢN HỒI ===\n");
        prompt.append("ĐIỂM KỸ THUẬT: [số]\n");
        if (metrics.getAverageHeartRate() != null) {
            prompt.append("ĐIỂM SỨC BỀN: [số]\n");
        }
        prompt.append("ĐIỂM TỔNG HỢP: [số]\n\n");
        prompt.append("ĐIỂM MẠNH: [nội dung]\n\n");
        prompt.append("ĐIỂM YẾU: [nội dung]\n\n");
        prompt.append("KHUYẾN NGHỊ: [nội dung]\n\n");

        return prompt.toString();
    }

    private WorkoutFeedbackAnalysisResponse parseWorkoutFeedbackAI(String rawResponse, WorkoutFeedbackAIRequest request) {
        try {
            log.info("Parsing AI workout feedback response");

            // Extract scores
            Double formScore = extractScore(rawResponse, "ĐIỂM KỸ THUẬT", request.getMistakeSummary().getTotalMistakes());
            Double enduranceScore = null;
            if (request.getSessionMetrics().getAverageHeartRate() != null) {
                enduranceScore = extractScore(rawResponse, "ĐIỂM SỨC BỀN", 0);
            }
            Double overallScore = extractScore(rawResponse, "ĐIỂM TỔNG HỢP", 0);

            // Extract narrative sections
            String strengths = extractSection(rawResponse, "ĐIỂM MẠNH");
            String weaknesses = extractSection(rawResponse, "ĐIỂM YẾU");
            String recommendations = extractSection(rawResponse, "KHUYẾN NGHỊ");

            return WorkoutFeedbackAnalysisResponse.builder()
                    .totalMistakes(request.getMistakeSummary().getTotalMistakes())
                    .formScore(formScore)
                    .enduranceScore(enduranceScore)
                    .overallScore(overallScore)
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .recommendations(recommendations)
                    .aiModel(geminiConfig.getModel())
                    .analyzedAt(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Error parsing AI workout feedback: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse workout feedback: " + e.getMessage(), e);
        }
    }
}


