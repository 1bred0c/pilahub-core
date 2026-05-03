📄 TÀI LIỆU TÍCH HỢP AI MODEL: PLANK (PRO VERSION)
1. TỔNG QUAN BẢN CẬP NHẬT
Bản model này đã được thiết kế lại hoàn toàn kiến trúc để bắt lỗi Plank với độ chính xác tuyệt đối (giải quyết triệt để lỗi báo sai tay/chân không rõ lý do của bản cũ).

Sự thay đổi về Interface:

Input cũ: 132 chiều (chỉ chứa tọa độ raw).

Input MỚI: 144 chiều (bao gồm 132 tọa độ đã được chuẩn hóa bất biến với khoảng cách Camera + 12 góc vật lý của cơ thể).

Logic MỚI: Kết hợp AI Model + Lớp Rule-based (Quy tắc vật lý) để quyết định kết quả cuối cùng.

2. DANH SÁCH FILE CẦN THIẾT
FE sẽ nhận được một file ZIP chứa các tệp sau:

pose_correction_exercise_aware.tflite: File model AI (đã update).

label_mappings.json: File map index đầu ra sang tên lỗi dạng text.

feature_scaler.pkl (hoặc scaler_params.json): Thông số chuẩn hóa (StandardScaler) BẮT BUỘC phải chạy trước khi đưa data vào model.

3. LUỒNG XỬ LÝ (PIPELINE) MỚI DÀNH CHO FE
Thay vì lấy trực tiếp tọa độ ném vào Model như trước, FE cần thực hiện theo luồng 4 bước sau cho mỗi Frame hình:

Bước 1: Trích xuất tọa độ thô (Raw Keypoints)
Từ MediaPipe, trích xuất 33 điểm (landmarks). Với mỗi điểm lấy đủ 4 giá trị [x, y, z, visibility].
👉 Đầu ra Bước 1: Mảng 132 phần tử.

Bước 2: Feature Engineering (Tính góc & Chuẩn hóa)
Đưa mảng 132 phần tử qua hàm preprocess_for_inference (đã cung cấp sẵn code Python mẫu).
Hàm này thực hiện 2 việc:

Dời tâm tọa độ về giữa Hông và chia cho khoảng cách Vai-Hông để bất biến với khoảng cách đứng gần/xa của người dùng.

Tính toán 12 góc quan trọng (Góc vai, khuỷu tay, lưng, cổ...).
👉 Đầu ra Bước 2: Mảng 144 phần tử.

Bước 3: Đưa qua Scaler & Chạy Model AI
Dùng StandardScaler (load từ file) biến đổi mảng 144 phần tử trên.

Truyền mảng 144 phần tử đã scale vào keypoints_input của TFLite model.

exercise_input truyền vào mảng dummy 8 phần tử: [1.0, 0, 0, 0, 0, 0, 0, 0].
👉 Đầu ra Bước 3: Index của lỗi (body_part).

Bước 4: Lớp lọc Rule-based Override (QUAN TRỌNG NHẤT)
Không tin tưởng 100% vào AI, đưa kết quả của AI và góc thô (tính ở Bước 2) qua lớp kiểm duyệt:

Nếu góc Lưng (Shoulder-Hip-Ankle) < 165 độ ➔ Báo lỗi Hips (Võng lưng/Nhô mông).

Nếu góc Cổ (Ear-Shoulder-Hip) < 150 độ ➔ Báo lỗi Neck (Cúi đầu).

Nếu góc bình thường ➔ Trả về kết quả dự đoán của AI.

4. CODE MẪU THAM KHẢO CHO FE
FE vui lòng tham khảo luồng logic chuẩn thông qua mã Python dưới đây để convert sang ngôn ngữ hiện tại của dự án (JS/TS, Dart, Swift...):

Python
# --- 1. THAM SỐ CẤU HÌNH ---
BODY_ANGLE_THRESHOLD = 165  
NECK_ANGLE_THRESHOLD = 150  

# --- 2. HÀM TÍNH GÓC ---
def get_xy(kps, idx):
    # Chỉ lấy X và Y để tránh nhiễu trục Z của MediaPipe
    return np.array([kps[idx*4], kps[idx*4+1]])

def calc_angle(a, b, c):
    ba = a - b; bc = c - b
    cos_a = np.dot(ba, bc) / (np.linalg.norm(ba)*np.linalg.norm(bc)+1e-8)
    return np.degrees(np.arccos(np.clip(cos_a, -1.0, 1.0)))

# --- 3. HÀM LỌC KẾT QUẢ AI CUỐI CÙNG ---
def rule_based_override(raw_angles, model_predicted_error):
    body_angle = (raw_angles[0] + raw_angles[1]) / 2  # Góc Lưng (Trái + Phải)
    neck_angle = (raw_angles[6] + raw_angles[7]) / 2  # Góc Cổ (Trái + Phải)
    
    if body_angle < BODY_ANGLE_THRESHOLD:
        return 'Hips'   
    if neck_angle < NECK_ANGLE_THRESHOLD:
        return 'Neck'   
    return model_predicted_error

# --- 4. LUỒNG CHẠY CHÍNH MỖI FRAME ---
def process_frame(raw_kps_from_mediapipe):
    # 1. Tính toán features & angles (Giống hệt code FE Python)
    features_144_array, raw_angles = preprocess_for_inference(raw_kps_from_mediapipe)
    
    # 2. Scale data (Dùng StandardScaler)
    scaled_features = scaler.transform([features_144_array])
    
    # 3. Model Inference
    # AI sẽ trả về lỗi: 'Hips', 'Neck', 'Arms', hoặc 'none'
    ai_predicted_error = run_tflite_model(scaled_features) 
    
    # 4. Chốt kết quả
    final_error = rule_based_override(raw_angles, ai_predicted_error)
    
    if final_error == 'none':
        return "CHUẨN"
    else:
        return f"SAI - Lỗi: {final_error}"