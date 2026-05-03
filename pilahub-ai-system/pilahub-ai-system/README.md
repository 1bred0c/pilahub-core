# 🏋️ Pilahub AI System

Hệ thống AI đánh giá sức khỏe và thể lực sử dụng Google Gemini API.

---

## 📋 Mục lục

- [Tổng quan](#tổng-quan)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt](#cài-đặt)
- [Chạy ứng dụng](#chạy-ứng-dụng)
- [Documentation](#documentation)
- [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
- [Testing](#testing)

---

## 🎯 Tổng quan

**Pilahub AI System** là một microservice phân tích và đánh giá hồ sơ sức khỏe người dùng bằng AI, cung cấp:

- ✅ **Đánh giá tổng thể**: Điểm số 0-100 và phân loại mức độ (POOR, AVERAGE, GOOD, EXCELLENT)
- ✅ **Phân tích chi tiết**: Điểm mạnh (highlights), rủi ro (risks), giải thích (explanations)
- ✅ **Khuyến nghị cá nhân**: Training, Nutrition, Lifestyle, Injury Prevention
- ✅ **Tiếng Việt**: Tất cả nội dung trả về bằng tiếng Việt
- ✅ **AI-Powered**: Sử dụng Google Gemini 3 Flash Preview

### Luồng hoạt động

```
Client Request → Controller → Service Layer → Gemini AI → Filter → Response
```

---

## 🛠️ Công nghệ sử dụng

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17+ | Runtime |
| Spring Boot | 3.5.10 | Framework |
| Maven | 3.9+ | Build tool |
| Google GenAI SDK | 1.0.0 | Gemini API client |
| Jackson | 2.19.4 | JSON processing |
| Lombok | - | Reduce boilerplate |
| SLF4J | - | Logging |

---

## 💻 Yêu cầu hệ thống

### Minimum Requirements

- **Java**: 17 hoặc cao hơn
- **Maven**: 3.9+
- **RAM**: 512MB+
- **OS**: Windows, Linux, macOS

### API Key

Bạn cần có **Google Gemini API Key**:

1. Truy cập: https://aistudio.google.com/app/apikey
2. Tạo API key miễn phí
3. Copy key để sử dụng

---

## 🚀 Cài đặt

### 1. Clone Repository

```bash
git clone <repository-url>
cd pilahub-ai-system
```

### 2. Cấu hình Environment Variable

Tạo file `.env` từ template:

```bash
# Windows PowerShell
copy .env.template .env

# Linux/Mac
cp .env.template .env
```

Mở file `.env` và thêm API key:

```env
GOOGLE_API_KEY=your-gemini-api-key
```

> ⚠️ **Lưu ý:** Tên biến PHẢI là `GOOGLE_API_KEY` (theo yêu cầu của Gemini Client)

### 3. Set Environment Variable

#### Windows PowerShell
```powershell
$env:GOOGLE_API_KEY="your-api-key"
```

#### Linux/Mac
```bash
export GOOGLE_API_KEY="your-api-key"
```

### 4. Build Project

```bash
# Clean và build
mvn clean install

# Hoặc skip tests
mvn clean install -DskipTests
```

---

## ▶️ Chạy ứng dụng

### Cách 1: Dùng Maven

```bash
mvn spring-boot:run
```

### Cách 2: Dùng JAR

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/pilahub-ai-system-0.0.1-SNAPSHOT.jar
```

### Cách 3: Run trong IDE

1. Mở project trong IntelliJ IDEA / Eclipse
2. Set environment variable `GOOGLE_API_KEY`
3. Run `PilahubAiSystemApplication.java`

### Kiểm tra Service đã chạy

```bash
# Health check
curl http://localhost:8080/api/v1/health-assessment/health
```

Response: `"Health Assessment Service is running"`

---

## 📚 Documentation

| Document | Mô tả | Link |
|----------|-------|------|
| **API Documentation** | Hướng dẫn sử dụng API đầy đủ | [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) |
| **Body Parts Reference** | Danh sách 22 body parts được hỗ trợ | [BODY_PARTS_REFERENCE.md](./BODY_PARTS_REFERENCE.md) |
| **Architecture** | Kiến trúc hệ thống chi tiết | [ARCHITECTURE.md](./ARCHITECTURE.md) |
| **API Testing** | Hướng dẫn test API | [API_TESTING.md](./API_TESTING.md) |
| **InBody Agent Guide** | Hướng dẫn tích hợp endpoint trích xuất InBody cho server chính | [INBODY_EXTRACTION_AGENT_GUIDE.md](./INBODY_EXTRACTION_AGENT_GUIDE.md) |
| **Roadmap Review AI Guide** | Hướng dẫn tích hợp nhận xét Roadmap cho server chính | [ROADMAP_REVIEW_AI_GUIDE.md](./ROADMAP_REVIEW_AI_GUIDE.md) |

### Quick Links

- 📖 [Cách gọi API](./API_DOCUMENTATION.md#api-endpoints)
- 📊 [Input Schema](./API_DOCUMENTATION.md#input-schema)
- 📤 [Output Schema](./API_DOCUMENTATION.md#output-schema)
- 🧍 [Body Parts List](./BODY_PARTS_REFERENCE.md#danh-sách-đầy-đủ-22-body-parts)
- ❌ [Error Handling](./API_DOCUMENTATION.md#error-handling)
- 📄 [InBody Integration Guide](./INBODY_EXTRACTION_AGENT_GUIDE.md)
- 🧭 [Roadmap Review AI Guide](./ROADMAP_REVIEW_AI_GUIDE.md)

---

## 🏗️ Kiến trúc hệ thống

### Package Structure

```
pilahub/
├── config/              # Configuration classes
│   ├── GeminiClientConfig    # Gemini Client bean
│   ├── GeminiConfig          # API settings
│   └── JacksonConfig         # JSON configuration
│
├── controller/          # REST Controllers
│   └── HealthAssessmentController
│
├── dto/                 # Data Transfer Objects
│   ├── HealthProfileRequest
│   ├── InjuryDTO
│   ├── AffectedBodyPart
│   └── response/
│       ├── HealthProfileAssessmentResponse
│       ├── HighlightDTO
│       ├── RiskDTO
│       └── RecommendationsDTO
│
├── enums/               # Enumerations
│   ├── AIModel
│   ├── HealthProfileLevel
│   ├── InjuryStatus
│   ├── MetricSource
│   ├── RiskSeverity
│   ├── WorkoutFrequency
│   └── WorkoutLevel
│
├── exception/           # Exception handling
│   ├── ErrorResponse
│   └── GlobalExceptionHandler
│
└── service/             # Business logic
    ├── GeminiAIService          # AI service interface
    ├── PromptBuilderService      # Prompt generation interface
    ├── ResponseFilterService     # Response parsing interface
    └── impl/
        ├── GeminiAIServiceImpl
        ├── PromptBuilderServiceImpl
        └── ResponseFilterServiceImpl
```

### Component Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ POST /api/v1/health-assessment/assess
       ↓
┌──────────────────────────┐
│ HealthAssessmentController│
└──────────┬───────────────┘
           │
           ↓
┌──────────────────────┐
│ GeminiAIService      │
└──────┬───────────────┘
       │
       ├→ PromptBuilderService → Build Vietnamese prompt
       │
       ├→ Gemini API Call → Google Gemini
       │
       └→ ResponseFilterService → Parse & validate JSON
           │
           ↓
┌──────────────────────────────┐
│ HealthProfileAssessmentResponse│
└──────────────────────────────┘
```

---

## 🧪 Testing

### Test thủ công với cURL

```bash
curl -X POST http://localhost:8080/api/v1/health-assessment/assess \
  -H "Content-Type: application/json" \
  -d '{
    "age": 25,
    "gender": "MALE",
    "workoutLevel": "BEGINNER",
    "workoutFrequency": "LIGHT",
    "injuries": [],
    "heightCm": 175,
    "weightKg": 70,
    "bmi": 22.86,
    "bodyFatPercentage": 18,
    "muscleMassKg": 32.5,
    "waistCm": 82,
    "hipCm": 96,
    "source": "GoogleFit"
  }'
```

### Test với sample request

```bash
# Sử dụng file sample có sẵn
curl -X POST http://localhost:8080/api/v1/health-assessment/assess \
  -H "Content-Type: application/json" \
  -d @sample-request.json
```

### Run Unit Tests

```bash
mvn test
```

### Run với Coverage

```bash
mvn clean test jacoco:report
```

---

## ⚙️ Configuration

### application.properties

```properties
# Server
server.port=8080

# Gemini API
gemini.api.key=${GOOGLE_API_KEY:}
gemini.api.model=gemini-3-flash-preview
gemini.api.temperature=0.7
gemini.api.max-tokens=4096
gemini.api.timeout-seconds=60
```

### Các Model Gemini có thể dùng

- `gemini-3-flash-preview` (mặc định, nhanh nhất)
- `gemini-2.5-flash-preview`
- `gemini-2.0-flash-exp`
- `gemini-pro`

---

## 🐛 Troubleshooting

### Lỗi: "API key must be provided"

**Nguyên nhân:** Chưa set environment variable

**Giải pháp:**
```bash
# Windows
$env:GOOGLE_API_KEY="your-key"

# Linux/Mac
export GOOGLE_API_KEY="your-key"
```

### Lỗi: "Cannot coerce empty String"

**Nguyên nhân:** Gửi `""` cho field object

**Giải pháp:** Sử dụng `{}` hoặc bỏ field đó

### Lỗi: Port 8080 đã được sử dụng

**Giải pháp:** Đổi port trong `application.properties`:
```properties
server.port=8081
```

---

## 📊 Performance

| Metric | Value |
|--------|-------|
| Startup time | ~3-5 giây |
| Response time (simple) | 2-4 giây |
| Response time (complex) | 4-8 giây |
| Memory usage | ~300MB |

---

## 🔒 Security Notes

- ⚠️ **Không commit API key** vào Git
- ✅ Sử dụng environment variables
- ✅ File `.env` đã được thêm vào `.gitignore`
- ✅ Validate tất cả input từ client
- ✅ Implement rate limiting nếu deploy production

---

## 🚢 Deployment

### Docker (Coming soon)

```bash
# Build image
docker build -t pilahub-ai-system .

# Run container
docker run -e GOOGLE_API_KEY=your-key -p 8080:8080 pilahub-ai-system
```

### Cloud Deployment

Hỗ trợ deploy lên:
- ☁️ AWS (EC2, ECS, Lambda)
- ☁️ Google Cloud (Cloud Run, App Engine)
- ☁️ Azure (App Service)
- ☁️ Heroku

---

## 📝 Changelog

### Version 1.0.0 (2026-01-31)

- ✨ Initial release
- ✅ Integration với Gemini API
- ✅ Health profile assessment endpoint
- ✅ Vietnamese content support
- ✅ 22 body parts constraint
- ✅ Comprehensive documentation

---

## 👥 Team

**Project:** Pilahub AI System  
**Organization:** Pilahub  
**Contact:** [Contact Info]

---

## 📄 License

[Your License] © 2026 Pilahub

---

## 🙏 Acknowledgments

- Google Gemini API
- Spring Boot Team
- Open Source Community

---

**Happy Coding! 🚀**
