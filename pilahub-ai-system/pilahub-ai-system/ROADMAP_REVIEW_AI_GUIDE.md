# Roadmap Review AI Integration Guide

Tài liệu này hướng dẫn server chính gọi AI service để **nhận xét/đánh giá Roadmap** dựa trên dữ liệu thực thi và thay đổi sức khỏe.

---

## 1) Endpoint chính

- **POST** `/api/v1/roadmap-review/analyze`
- **Content-Type:** `application/json`

### Request JSON mẫu

```json
{
  "roadmap": {
    "roadmapId": "9f264d71-9b96-4bb2-8b7f-f4f4ed0ccf88",
    "title": "12-week Fat Loss + Core Strength",
    "description": "Roadmap generated for fat loss and conditioning",
    "startDate": "2026-01-10T00:00:00Z",
    "endDate": "2026-04-10T00:00:00Z",
    "progressPercent": 100,
    "status": "IN_PROGRESS",
    "source": "AI_GENERATED",
    "goals": [
      {
        "goalId": "e3432d60-4c3f-4e76-bdd7-ea12f2e6956d",
        "code": "FAT_LOSS",
        "name": "Giảm mỡ",
        "isPrimary": true,
        "goalOrder": 1
      },
      {
        "goalId": "f81944f9-9ec1-4c17-81d8-8bc3c5b8d7aa",
        "code": "IMPROVE_ENDURANCE",
        "name": "Tăng sức bền",
        "isPrimary": false,
        "goalOrder": 2
      }
    ],
    "initialHealthProfileId": "08d9e67b-d0b1-4458-9f83-1f7fbe3ab4ec",
    "finalHealthProfileId": "47b2e6d1-0f6e-437f-a4fd-41f4a475e2bf"
  },
  "initialHealthProfile": {
    "healthProfileId": "08d9e67b-d0b1-4458-9f83-1f7fbe3ab4ec",
    "createdAt": "2026-01-09T09:15:00Z",
    "heightCm": 170.0,
    "weightKg": 78.5,
    "bmi": 27.2,
    "bodyFatPercentage": 29.4,
    "muscleMassKg": 29.8,
    "waistCm": 91.0,
    "hipCm": 102.0,
    "source": "InBody",
    "metadata": "{\"device\":\"InBody 270\"}"
  },
  "finalHealthProfile": {
    "healthProfileId": "47b2e6d1-0f6e-437f-a4fd-41f4a475e2bf",
    "createdAt": "2026-04-11T08:50:00Z",
    "heightCm": 170.0,
    "weightKg": 73.2,
    "bmi": 25.3,
    "bodyFatPercentage": 24.1,
    "muscleMassKg": 30.6,
    "waistCm": 84.0,
    "hipCm": 98.0,
    "source": "Manual",
    "metadata": "{\"note\":\"post-roadmap check\"}"
  },
  "traineeContext": {
    "age": 27,
    "gender": "MALE",
    "workoutFrequency": 4
  },
  "executionSummary": {
    "totalSchedules": 48,
    "completedSchedules": 43,
    "totalExercises": 320,
    "completedExercises": 286,
    "completionRate": 0.8938
  }
}
```

### Response JSON mẫu

```json
{
  "overallScore": 87,
  "subScores": {
    "effectiveness": 90,
    "adherence": 89,
    "bodyCompositionChange": 87,
    "muscleChange": 100,
    "waistChange": 77,
    "goalAchievement": 100,
    "safetyRisk": 100
  },
  "deltaMetrics": {
    "weightKg": {"baseline": 78.5, "final": 73.2, "percent": -6.8},
    "bodyFat%": {"baseline": 29.4, "final": 24.1, "percent": -18.0},
    "muscleMassKg": {"baseline": 29.8, "final": 30.6, "percent": 2.7},
    "waistCm": {"baseline": 91, "final": 84, "percent": -7.7}
  },
  "narrativeSummary": "Người dùng giảm 5.3 kg (từ 78.5 kg xuống 73.2 kg, tương đương -6.8%), giảm 5.3 điểm % mỡ cơ thể (-18.0% so với mốc ban đầu), và tăng 0.8 kg cơ. Họ hoàn thành 89.4% lịch tập và 89.4% bài tập, cho thấy mức tuân thủ rất cao. Dữ liệu cho thấy roadmap đạt mục tiêu chính (giảm mỡ) rõ rệt, đồng thời tăng cơ, cho thấy cải thiện thành phần cơ thể xuất sắc. Đánh giá tổng thể: hiệu quả cao, rủi ro sức khỏe thấp, roadmap này thành công trong việc giảm mỡ và duy trì/giá tăng cơ. Nhược điểm: không có thông tin về cải thiện sức bền nên mục tiêu phụ chưa đánh giá được chính xác. Confidence level cao (ví dụ: 0.85), vì các chỉ số chủ yếu đều có trong dữ liệu.",
  "prioritizedRecommendations": [
    {
      "recommendation": "Duy trì lượng đạm cao (>1.3 g/kg/ngày) trong chế độ ăn",
      "rationale": "Nghiên cứu cho thấy ăn đạm cao giúp bảo toàn và tăng cơ trong khi giảm mỡ【35†L200-L208】, ngăn ngừa mất cơ trong quá trình giảm cân."
    },
    {
      "recommendation": "Tiếp tục lịch tập hiện tại, thêm các buổi cardio hoặc bài tập sức bền",
      "rationale": "Tần suất tập 4 lần/tuần hiện đã tốt, thêm các buổi cardio/sức bền để hỗ trợ mục tiêu tăng sức bền (hiện chưa đo trực tiếp) và cải thiện thành phần cơ thể."
    },
    {
      "recommendation": "Đặt mục tiêu duy trì tiến triển từ từ (5-10% giảm cân/3 tháng)",
      "rationale": "Theo khuyến cáo sức khỏe, giảm 5-10% trọng lượng ban đầu mang lại lợi ích sức khỏe lớn【24†L15-L23】【39†L181-L184】. Hãy tiếp tục duy trì hành vi lành mạnh để ổn định kết quả."
    }
  ],
  "confidenceLevel": 85
}
```

---

## 2) Tài liệu tham khảo cho Roadmap Review

Tính năng này hỗ trợ upload tài liệu tham khảo (PDF/Markdown/Text) để AI bám theo tiêu chí chấm điểm.

### Upload tài liệu

- **POST** `/api/v1/admin/documents/upload-roadmap-review-reference`
- **Content-Type:** `multipart/form-data`
- Field: `file`

### Kiểm tra trạng thái

- **GET** `/api/v1/admin/documents/roadmap-review-reference/status`

### Download tài liệu

- **GET** `/api/v1/admin/documents/download-roadmap-review-reference`

### Xóa tài liệu

- **DELETE** `/api/v1/admin/documents/roadmap-review-reference/{fileName}`

---

## 3) Gợi ý tích hợp cho server chính

1. Gửi đầy đủ dữ liệu `roadmap`, `initialHealthProfile`, `finalHealthProfile`, `traineeContext`, `executionSummary`.
2. Đảm bảo `completionRate` và các số đo có giá trị hợp lệ để AI tính điểm chính xác.
3. Nếu có tài liệu chấm điểm, upload trước để AI dùng tiêu chí chuẩn.
4. Kết quả trả về đúng schema JSON như trên để server chính parse trực tiếp.

---

## 4) Health check

- **GET** `/api/v1/roadmap-review/health`
- Response: `"Roadmap Review Service is running"`

