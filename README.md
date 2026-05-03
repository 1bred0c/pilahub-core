# 🧘‍♀️ PilaHub - Nền tảng Tập luyện Pilates & Thương mại Điện tử Đa nền tảng

![Java Spring Boot](https://img.shields.io/badge/Backend-Java_Spring_Boot-green?logo=spring)
![React Native](https://img.shields.io/badge/Mobile-React_Native-blue?logo=react)
![Next.js](https://img.shields.io/badge/Web-Next.js-black?logo=next.js)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue?logo=postgresql)
![GCP](https://img.shields.io/badge/Cloud-Google_Cloud_Platform-orange?logo=google-cloud)
![AI](https://img.shields.io/badge/Feature-AI_Coaching_%&-success)
![IoT](https://img.shields.io/badge/Feature-IoT_Tracking-critical)

> **"Hệ sinh thái Pilates toàn diện kết hợp Web, Mobile App, Trí tuệ nhân tạo (AI), Dữ liệu IoT và Nền tảng E-Commerce."**

[Chèn hình ảnh: Banner/Mockup của PilaHub gồm cả màn hình Mobile và Web Dashboard]

---

## 📖 Giới thiệu Dự án (Product Vision)
**PilaHub** ra đời nhằm giải quyết sự phân mảnh trong hệ sinh thái sức khỏe kỹ thuật số. Người tập Pilates thường phải dùng nhiều ứng dụng khác nhau để tìm bài tập, thuê huấn luyện viên, và mua sắm thiết bị. PilaHub gom tất cả vào một nền tảng All-in-One:
* **Với người tập (Trainee):** Cung cấp lộ trình cá nhân hóa nhờ AI, tích hợp thiết bị IoT đo nhịp tim thời gian thực và hỗ trợ chỉnh sửa tư thế bằng AI.
* **Với Huấn luyện viên (Coach):** Môi trường để quản lý học viên, tinh chỉnh lộ trình tập luyện và dạy học trực tuyến 1-1.
* **Với Nhà bán hàng (Vendor):** Sàn thương mại điện tử chuyên dụng (Niche Marketplace) cung cấp dụng cụ, thực phẩm chức năng Pilates.

---

## 🛠 Nền tảng Công nghệ (Tech Stack)

Hệ thống được phát triển với tư duy Full-stack, tối ưu cho từng nền tảng người dùng:

### 1. Client Side (Presentation Layer)
* **Mobile App (React Native):** Dành riêng cho **Trainee** và **Coach**. Xử lý luồng Camera AI (Posture Detection), kết nối Bluetooth với thiết bị IoT và gọi Video trực tuyến.
* **Web App (Next.js):** Dành cho **Admin** và **Vendor**. Dashboard chuyên biệt giúp quản lý đơn hàng, theo dõi doanh thu và cấu hình hệ thống (Complex UI/UX).

### 2. Core System (Backend & Database)
* **Framework:** **Java Spring Boot** (Monolithic) xử lý toàn bộ Business Logic cốt lõi.
* **Database:** **PostgreSQL** chuẩn hóa cao, chia thành 5 module lớn (E-Commerce, Booking, Health/AI...).
* **Task Scheduling:** Tích hợp Cron Jobs tự động dọn dẹp OTP, cập nhật trạng thái đơn hàng, và phân bổ doanh thu ví (Vendor Payout).

### 3. Cloud & Infrastructure
* **Google Cloud Platform (GCP):** Toàn bộ Core System được deploy trên GCE (Google Compute Engine).
* **NGINX:** Reverse Proxy xử lý Load Balancing, SSL, điều phối HTTPS và WebSockets.

### 4. Third-Party Integrations
* **Live Streaming:** **Agora** (Hỗ trợ gọi Video Call 1-1 chuẩn thời gian thực).
* **Trí tuệ nhân tạo:** **Gemini File Search & LLM** (Tự động thiết kế lộ trình tập luyện - Roadmap).
* **Payment & Logistics:** Cổng thanh toán **VNPay / MoMo** và Giao hàng nhanh (**GHN** API).
* **Push Notifications:** **Firebase Cloud Messaging (FCM)**.

---

## 🚀 Tính năng nổi bật (Key Features)

### 🤸 Trải nghiệm Học viên (Mobile)
* **AI-Assisted Training:** Phân tích tư thế tập theo thời gian thực (Real-time tracking & AI feedback). [Chèn ảnh/GIF màn hình AI vẽ Skeleton lên người tập]
* **IoT Health Tracking:** Đồng bộ hóa dữ liệu nhịp tim và nhịp thở liên tục thông qua thiết bị đeo tay.
* **AI Personalized Roadmap:** Kết hợp phân tích hình thể (BodyGram) để AI tự động sinh lộ trình tập (Roadmap) tối ưu nhất, có thể được HLV tinh chỉnh lại.
* **Marketplace:** Đặt hàng, theo dõi tiến trình vận chuyển (GHN) và thanh toán e-wallet ngay trên di động.

### 🧑‍🏫 Trải nghiệm Huấn luyện viên (Mobile)
* **Quản lý lịch dạy:** Thiết lập lịch trống (Coach Availability) và nhận Booking.
* **Live Coaching Session:** Dạy học trực tuyến 1-1 qua Video (Agora), gửi Feedback ngay sau buổi tập.
* **Custom Roadmap:** Theo dõi sát sao tiến độ và thay đổi lộ trình AI đề xuất để phù hợp hơn với thực tế học viên.

### 🏪 Quản trị Vendor & Admin (Web Next.js)
* **Shop Dashboard:** Quản lý kho sản phẩm, theo dõi đơn hàng, thống kê lợi nhuận trực quan.
* **Wallet & Payout:** Hệ thống đối soát doanh thu tự động, cho phép Vendor rút tiền từ hệ thống (Withdrawal).
* **Admin Panel:** Duyệt Vendor, quản lý bài tập (Exercises/Courses), theo dõi luồng giao dịch.

---

## 🏗 Kiến trúc Hệ thống & Cơ sở dữ liệu

[Chèn hình ảnh: Sơ đồ System Architecture (Hình 1.1 trong Report 4)]

**Sơ đồ Kiến trúc:** Hệ thống nhận luồng Request từ Client. Các Request RESTful API và WebSockets (Live chat/Tracking) sẽ được NGINX phân luồng. Java Spring Boot Backend xử lý logic và gọi sang System AI hoặc các External APIs (Agora, GHN, VNPay...).

[Chèn hình ảnh: Sơ đồ Database ERD hoặc sơ đồ chia 5 Modules (Hình 2 trong Report 4)]

**Thiết kế Dữ liệu:** Database được thiết kế chuyên sâu thành 5 Modules độc lập để dễ dàng bảo trì và mở rộng:
1. `Users, Profile & Wallets`
2. `E-Commerce` (Cart, Orders, Shipping)
3. `Courses, Exercises & Supplements`
4. `Coaching, Booking & Roadmaps`
5. `AI, Health & Progress`

---

## 👥 Đội ngũ Phát triển

Dự án Capstone (Mã: SP26SE004) - Đại học FPT TP.HCM.
* **Trần Công Tường (Leader):** Backend Business Logic, Database Design & Integration.
* **Nguyễn Thanh Phong (Member):** Web/App Frontend Implementation & UI Testing.
* **Nguyễn Cao Trí (Member):** System Architecture, Backend Core (Controllers, Services) & Unit Testing.
* **Nguyễn Văn Minh Thoại (Member):** Front-end<img width="1024" height="1024" alt="3b8cae1ea37922277b68" src="https://github.com/user-attachments/assets/fe31d591-6c5d-4047-9bad-2de290f4e91a" />

* **Nguyễn Thanh Mai (Member):** Front-end
* **Giảng viên hướng dẫn:** ThS. Đỗ Tấn Nhàn

---
*Cảm ơn bạn đã quan tâm đến hệ sinh thái PilaHub!*
