package pilahub.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pilahub.dto.HealthProfileRequest;
import pilahub.dto.InjuryDTO;
import pilahub.service.PromptBuilderService;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptBuilderServiceImpl implements PromptBuilderService {

    @Override
    public String buildHealthAssessmentPromptWithFile(HealthProfileRequest request, String fileUri) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Bạn là một chuyên gia sức khỏe và thể hình với kiến thức chuyên sâu về đánh giá sức khỏe và lập kế hoạch tập luyện.\n\n");

        prompt.append("# THAM KHẢO QUY ĐỊNH CHẤM ĐIỂM\n");
        prompt.append("📎 File quy định: ").append(fileUri).append("\n");
        prompt.append("Vui lòng ĐỌC KỸ nội dung file quy định chấm điểm ở trên để áp dụng đúng công thức và tiêu chí đánh giá.\n\n");

        prompt.append("# NHIỆM VỤ\n");
        prompt.append("Phân tích thông tin sức khỏe sau và trả về một đánh giá toàn diện bằng TIẾNG VIỆT theo định dạng JSON được chỉ định.\n");
        prompt.append("ÁP DỤNG CHÍNH XÁC các quy tắc và công thức trong file quy định đính kèm.\n\n");

        prompt.append("# THÔNG TIN NGƯỜI DÙNG\n");
        prompt.append("- Tuổi: ").append(request.getAge()).append(" tuổi\n");
        prompt.append("- Giới tính: ").append(request.getGender()).append("\n");
        prompt.append("- Cân nặng: ").append(request.getWeightKg()).append(" kg\n");
        prompt.append("- Chiều cao: ").append(request.getHeightCm()).append(" cm\n");

        if (request.getBmi() != null) {
            prompt.append("- BMI: ").append(request.getBmi()).append("\n");
        }

        if (request.getBodyFatPercentage() != null) {
            prompt.append("- Tỷ lệ mỡ cơ thể: ").append(request.getBodyFatPercentage()).append("%\n");
        }

        if (request.getMuscleMassKg() != null) {
            prompt.append("- Khối lượng cơ: ").append(request.getMuscleMassKg()).append(" kg\n");
        }

        if (request.getWaistCm() != null) {
            prompt.append("- Vòng eo: ").append(request.getWaistCm()).append(" cm\n");
        }

        if (request.getHipCm() != null) {
            prompt.append("- Vòng hông: ").append(request.getHipCm()).append(" cm\n");
        }

        prompt.append("- Trình độ tập luyện: ").append(getWorkoutLevelVietnamese(request.getWorkoutLevel())).append("\n");
        prompt.append("- Tần suất tập luyện: ").append(getWorkoutFrequencyVietnamese(request.getWorkoutFrequency())).append("\n");

        if (request.getInjuries() != null && !request.getInjuries().isEmpty()) {
            prompt.append("\n## CHẤN THƯƠNG\n");
            for (InjuryDTO injury : request.getInjuries()) {
                prompt.append("- ").append(injury.getName())
                      .append(" (Trạng thái: ").append(getInjuryStatusVietnamese(injury.getStatus())).append(")\n");
                if (injury.getDescription() != null) {
                    prompt.append("  Mô tả: ").append(injury.getDescription()).append("\n");
                }
                if (injury.getAffectedBodyParts() != null && !injury.getAffectedBodyParts().isEmpty()) {
                    prompt.append("  Vùng ảnh hưởng: ");
                    injury.getAffectedBodyParts().forEach(part ->
                        prompt.append(part.getName()).append(", ")
                    );
                    prompt.setLength(prompt.length() - 2);
                    prompt.append("\n");
                }
            }
        }

        prompt.append("\n# YÊU CẦU ĐỊNH DẠNG OUTPUT (JSON)\n");
        prompt.append("Trả về CHÍNH XÁC theo format JSON này, TẤT CẢ NỘI DUNG PHẢI BẰNG TIẾNG VIỆT:\n\n");
        prompt.append("{\n");
        prompt.append("  \"score\": <số điểm từ 0-100>,\n");
        prompt.append("  \"healthProfileLevel\": \"<POOR|AVERAGE|GOOD|EXCELLENT>\",\n");
        prompt.append("  \"highlights\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"<tiêu đề ngắn gọn bằng tiếng Việt>\",\n");
        prompt.append("      \"description\": \"<mô tả chi tiết bằng tiếng Việt>\",\n");
        prompt.append("      \"relatedMetrics\": [\"<tên chỉ số liên quan>\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"risks\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"riskType\": \"<INJURY|HEALTH|PERFORMANCE>\",\n");
        prompt.append("      \"severity\": \"<LOW|MODERATE|HIGH|CRITICAL>\",\n");
        prompt.append("      \"description\": \"<mô tả rủi ro bằng tiếng Việt>\",\n");
        prompt.append("      \"affectedBodyParts\": [\"<bộ phận cơ thể>\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"explanations\": {\n");
        prompt.append("    \"score_calculation\": \"<giải thích cách tính điểm bằng tiếng Việt - PHẢI NÊU RÕ CÔNG THỨC VÀ HỆ SỐ ĐÃ ÁP DỤNG THEO FILE QUY ĐỊNH>\",\n");
        prompt.append("    \"key_factors\": [\"<các yếu tố quan trọng>\"]\n");
        prompt.append("  },\n");
        prompt.append("  \"recommendations\": {\n");
        prompt.append("    \"training\": [\"<khuyến nghị về tập luyện bằng tiếng Việt>\"],\n");
        prompt.append("    \"nutrition\": [\"<khuyến nghị về dinh dưỡng bằng tiếng Việt>\"],\n");
        prompt.append("    \"lifestyle\": [\"<khuyến nghị về lối sống bằng tiếng Việt>\"],\n");
        prompt.append("    \"injuryPrevention\": [\"<khuyến nghị phòng chấn thương bằng tiếng Việt>\"]\n");
        prompt.append("  },\n");
        prompt.append("  \"confidenceScore\": <số từ 0.0 đến 1.0>\n");
        prompt.append("}\n\n");

        prompt.append("# HƯỚNG DẪN ĐÁNH GIÁ\n");
        prompt.append("ÁP DỤNG CHÍNH XÁC các quy tắc từ file quy định đính kèm:\n");
        prompt.append("1. Score (0-100): Tính theo CÔNG THỨC CHÍNH XÁC trong file quy định\n");
        prompt.append("2. HealthProfileLevel: Áp dụng ĐÚNG NGƯỠNG ĐIỂM trong file quy định\n");
        prompt.append("3. Highlights: Liệt kê 3-5 điểm mạnh nổi bật\n");
        prompt.append("4. Risks: Phân tích rủi ro theo TIÊU CHÍ trong file quy định\n");
        prompt.append("5. Recommendations: Đưa ra 4-6 khuyến nghị cụ thể cho mỗi danh mục\n");
        prompt.append("6. ConfidenceScore: Đánh giá độ tin cậy theo HƯỚNG DẪN trong file quy định\n\n");

        prompt.append("# DANH SÁCH BODY PARTS ĐƯỢC PHÉP\n");
        prompt.append("Khi liệt kê affectedBodyParts trong risks, CHỈ sử dụng các tên sau (bằng TIẾNG ANH):\n");
        prompt.append("- Head, Neck, Cervical Spine, Thoracic Spine, Lumbar Spine\n");
        prompt.append("- Core, Shoulders, Upper Back, Lower Back, Chest\n");
        prompt.append("- Upper Arms, Elbows, Forearms, Wrists, Hands\n");
        prompt.append("- Hips, Glutes, Thighs, Knees, Calves, Ankles, Feet\n");
        prompt.append("KHÔNG được sử dụng tên tiếng Việt hoặc tên khác ngoài danh sách trên!\n\n");

        prompt.append("# LƯU Ý QUAN TRỌNG\n");
        prompt.append("- PHẢI ĐỌC VÀ ÁP DỤNG file quy định chấm điểm đính kèm\n");
        prompt.append("- TẤT CẢ văn bản (title, description, recommendations) PHẢI bằng TIẾNG VIỆT\n");
        prompt.append("- CHỈ tên body parts trong affectedBodyParts phải bằng TIẾNG ANH (theo danh sách trên)\n");
        prompt.append("- Chỉ trả về JSON, KHÔNG thêm bất kỳ text nào khác\n");
        prompt.append("- Đảm bảo JSON hợp lệ và có thể parse được\n");
        prompt.append("- Trong explanations.score_calculation PHẢI giải thích rõ công thức đã áp dụng\n\n");

        prompt.append("BẮT ĐẦU PHÂN TÍCH VÀ TRẢ VỀ JSON:");

        log.debug("Built prompt with file reference, length: {} characters", prompt.length());
        return prompt.toString();
    }

    @Override
    public String buildHealthAssessmentPrompt(HealthProfileRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Bạn là một chuyên gia sức khỏe và thể hình với kiến thức chuyên sâu về đánh giá sức khỏe và lập kế hoạch tập luyện.\n\n");

        prompt.append("# NHIỆM VỤ\n");
        prompt.append("Phân tích thông tin sức khỏe sau và trả về một đánh giá toàn diện bằng TIẾNG VIỆT theo định dạng JSON được chỉ định.\n\n");

        prompt.append("# THÔNG TIN NGƯỜI DÙNG\n");
        prompt.append("- Tuổi: ").append(request.getAge()).append(" tuổi\n");
        prompt.append("- Giới tính: ").append(request.getGender()).append("\n");
        prompt.append("- Cân nặng: ").append(request.getWeightKg()).append(" kg\n");
        prompt.append("- Chiều cao: ").append(request.getHeightCm()).append(" cm\n");

        if (request.getBmi() != null) {
            prompt.append("- BMI: ").append(request.getBmi()).append("\n");
        }

        if (request.getBodyFatPercentage() != null) {
            prompt.append("- Tỷ lệ mỡ cơ thể: ").append(request.getBodyFatPercentage()).append("%\n");
        }

        if (request.getMuscleMassKg() != null) {
            prompt.append("- Khối lượng cơ: ").append(request.getMuscleMassKg()).append(" kg\n");
        }

        if (request.getWaistCm() != null) {
            prompt.append("- Vòng eo: ").append(request.getWaistCm()).append(" cm\n");
        }

        if (request.getHipCm() != null) {
            prompt.append("- Vòng hông: ").append(request.getHipCm()).append(" cm\n");
        }

        prompt.append("- Trình độ tập luyện: ").append(getWorkoutLevelVietnamese(request.getWorkoutLevel())).append("\n");
        prompt.append("- Tần suất tập luyện: ").append(getWorkoutFrequencyVietnamese(request.getWorkoutFrequency())).append("\n");

        if (request.getInjuries() != null && !request.getInjuries().isEmpty()) {
            prompt.append("\n## CHẤN THƯƠNG\n");
            for (InjuryDTO injury : request.getInjuries()) {
                prompt.append("- ").append(injury.getName())
                      .append(" (Trạng thái: ").append(getInjuryStatusVietnamese(injury.getStatus())).append(")\n");
                if (injury.getDescription() != null) {
                    prompt.append("  Mô tả: ").append(injury.getDescription()).append("\n");
                }
                if (injury.getAffectedBodyParts() != null && !injury.getAffectedBodyParts().isEmpty()) {
                    prompt.append("  Vùng ảnh hưởng: ");
                    injury.getAffectedBodyParts().forEach(part ->
                        prompt.append(part.getName()).append(", ")
                    );
                    prompt.setLength(prompt.length() - 2);
                    prompt.append("\n");
                }
            }
        }

        prompt.append("\n# YÊU CẦU ĐỊNH DẠNG OUTPUT (JSON)\n");
        prompt.append("Trả về CHÍNH XÁC theo format JSON này, TẤT CẢ NỘI DUNG PHẢI BẰNG TIẾNG VIỆT:\n\n");
        prompt.append("{\n");
        prompt.append("  \"score\": <số điểm từ 0-100>,\n");
        prompt.append("  \"healthProfileLevel\": \"<POOR|AVERAGE|GOOD|EXCELLENT>\",\n");
        prompt.append("  \"highlights\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"<tiêu đề ngắn gọn bằng tiếng Việt>\",\n");
        prompt.append("      \"description\": \"<mô tả chi tiết bằng tiếng Việt>\",\n");
        prompt.append("      \"relatedMetrics\": [\"<tên chỉ số liên quan>\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"risks\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"riskType\": \"<INJURY|HEALTH|PERFORMANCE>\",\n");
        prompt.append("      \"severity\": \"<LOW|MODERATE|HIGH|CRITICAL>\",\n");
        prompt.append("      \"description\": \"<mô tả rủi ro bằng tiếng Việt>\",\n");
        prompt.append("      \"affectedBodyParts\": [\"<bộ phận cơ thể>\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"explanations\": {\n");
        prompt.append("    \"score_calculation\": \"<giải thích cách tính điểm bằng tiếng Việt>\",\n");
        prompt.append("    \"key_factors\": [\"<các yếu tố quan trọng>\"]\n");
        prompt.append("  },\n");
        prompt.append("  \"recommendations\": {\n");
        prompt.append("    \"training\": [\"<khuyến nghị về tập luyện bằng tiếng Việt>\"],\n");
        prompt.append("    \"nutrition\": [\"<khuyến nghị về dinh dưỡng bằng tiếng Việt>\"],\n");
        prompt.append("    \"lifestyle\": [\"<khuyến nghị về lối sống bằng tiếng Việt>\"],\n");
        prompt.append("    \"injuryPrevention\": [\"<khuyến nghị phòng chấn thương bằng tiếng Việt>\"]\n");
        prompt.append("  },\n");
        prompt.append("  \"confidenceScore\": <số từ 0.0 đến 1.0>\n");
        prompt.append("}\n\n");

        prompt.append("# HƯỚNG DẪN ĐÁNH GIÁ\n");
        prompt.append("1. Score (0-100): Tính toán dựa trên BMI, tỷ lệ mỡ cơ thể, khối cơ, chấn thương, và mức độ hoạt động\n");
        prompt.append("2. HealthProfileLevel: \n");
        prompt.append("   - EXCELLENT (86-100): Sức khỏe xuất sắc\n");
        prompt.append("   - GOOD (71-85): Sức khỏe tốt\n");
        prompt.append("   - AVERAGE (51-70): Sức khỏe trung bình\n");
        prompt.append("   - POOR (0-50): Sức khỏe kém\n");
        prompt.append("3. Highlights: Liệt kê 3-5 điểm mạnh nổi bật\n");
        prompt.append("4. Risks: Phân tích các rủi ro tiềm ẩn về chấn thương hoặc sức khỏe\n");
        prompt.append("5. Recommendations: Đưa ra 4-6 khuyến nghị cụ thể cho mỗi danh mục\n");
        prompt.append("6. ConfidenceScore: Đánh giá độ tin cậy của phân tích (0.0-1.0)\n\n");

        prompt.append("# DANH SÁCH BODY PARTS ĐƯỢC PHÉP\n");
        prompt.append("Khi liệt kê affectedBodyParts trong risks, CHỈ sử dụng các tên sau (bằng TIẾNG ANH):\n");
        prompt.append("- Head, Neck, Cervical Spine, Thoracic Spine, Lumbar Spine\n");
        prompt.append("- Core, Shoulders, Upper Back, Lower Back, Chest\n");
        prompt.append("- Upper Arms, Elbows, Forearms, Wrists, Hands\n");
        prompt.append("- Hips, Glutes, Thighs, Knees, Calves, Ankles, Feet\n");
        prompt.append("KHÔNG được sử dụng tên tiếng Việt hoặc tên khác ngoài danh sách trên!\n\n");

        prompt.append("# LƯU Ý QUAN TRỌNG\n");
        prompt.append("- TẤT CẢ văn bản (title, description, recommendations) PHẢI bằng TIẾNG VIỆT\n");
        prompt.append("- CHỈ tên body parts trong affectedBodyParts phải bằng TIẾNG ANH (theo danh sách trên)\n");
        prompt.append("- Chỉ trả về JSON, KHÔNG thêm bất kỳ text nào khác\n");
        prompt.append("- Đảm bảo JSON hợp lệ và có thể parse được\n");
        prompt.append("- Sử dụng thuật ngữ y khoa và thể thao chính xác\n");
        prompt.append("- Đưa ra khuyến nghị thực tế, có thể áp dụng được\n\n");

        prompt.append("BẮT ĐẦU PHÂN TÍCH VÀ TRẢ VỀ JSON:");

        log.debug("Built prompt with length: {} characters", prompt.length());
        return prompt.toString();
    }

    private String getWorkoutLevelVietnamese(Enum<?> level) {
        if (level == null) return "Không xác định";
        switch (level.name()) {
            case "BEGINNER": return "Người mới bắt đầu";
            case "INTERMEDIATE": return "Trung cấp";
            case "ADVANCED": return "Nâng cao";
            default: return level.name();
        }
    }

    private String getWorkoutFrequencyVietnamese(Enum<?> frequency) {
        if (frequency == null) return "Không xác định";
        switch (frequency.name()) {
            case "SEDENTARY": return "Ít vận động (0 ngày/tuần)";
            case "LIGHT": return "Nhẹ (1-2 ngày/tuần)";
            case "MODERATE": return "Trung bình (3-4 ngày/tuần)";
            case "ACTIVE": return "Hoạt động (5-6 ngày/tuần)";
            case "ATHLETE": return "Vận động viên (7+ ngày/tuần)";
            default: return frequency.name();
        }
    }

    private String getInjuryStatusVietnamese(Enum<?> status) {
        if (status == null) return "Không xác định";
        switch (status.name()) {
            case "ACTIVE": return "Đang chấn thương";
            case "RECOVERED": return "Đã phục hồi";
            default: return status.name();
        }
    }
}
