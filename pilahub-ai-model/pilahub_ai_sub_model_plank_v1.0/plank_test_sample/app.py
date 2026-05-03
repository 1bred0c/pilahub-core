import tkinter as tk
from PIL import Image, ImageTk
import cv2
import mediapipe as mp
import numpy as np
import tensorflow as tf
import json
import os
import pickle

# --- CÁC HÀM PREPROCESSING BẠN VỪA VIẾT ---
LEFT_SHOULDER=11; RIGHT_SHOULDER=12; LEFT_ELBOW=13; RIGHT_ELBOW=14
LEFT_WRIST=15; RIGHT_WRIST=16; LEFT_HIP=23; RIGHT_HIP=24
LEFT_KNEE=25; RIGHT_KNEE=26; LEFT_ANKLE=27; RIGHT_ANKLE=28
LEFT_EAR=7; RIGHT_EAR=8; NOSE=0

# THÊM HÀM NÀY LÊN ĐẦU
BODY_ANGLE_THRESHOLD = 165  
NECK_ANGLE_THRESHOLD = 150  

def rule_based_override(raw_angles, model_body_part):
    # raw_angles[0], raw_angles[1] là Body_L, Body_R
    body_avg  = (raw_angles[0] + raw_angles[1]) / 2  
    # raw_angles[6], raw_angles[7] là Neck_L, Neck_R
    neck_avg  = (raw_angles[6] + raw_angles[7]) / 2  
    
    if body_avg < BODY_ANGLE_THRESHOLD:
        return 'Hips'   
    if neck_avg < NECK_ANGLE_THRESHOLD:
        return 'Neck'   
    return model_body_part

def get_xyz(kps, idx):
    return np.array([kps[idx*4], kps[idx*4+1], kps[idx*4+2]])

def get_xy_for_angle(kps, idx):
    return np.array([kps[idx*4], kps[idx*4+1]])

def calc_angle(a, b, c):
    ba = a - b; bc = c - b
    cos_a = np.dot(ba, bc) / (np.linalg.norm(ba)*np.linalg.norm(bc)+1e-8)
    return np.degrees(np.arccos(np.clip(cos_a, -1.0, 1.0)))

# SỬA HÀM NÀY ĐỂ RETURN THÊM GÓC THÔ
def preprocess_for_inference(raw_kps, scaler):
    kps = np.array(raw_kps)
    hip_c = (get_xyz(kps, LEFT_HIP) + get_xyz(kps, RIGHT_HIP)) / 2
    sh_c  = (get_xyz(kps, LEFT_SHOULDER) + get_xyz(kps, RIGHT_SHOULDER)) / 2
    scale = np.linalg.norm(sh_c - hip_c) + 1e-8
    norm = []
    for i in range(33):
        xyz = (get_xyz(kps, i) - hip_c) / scale
        norm.extend([xyz[0], xyz[1], xyz[2], kps[i*4+3]])
        
    angles = [
        calc_angle(get_xy_for_angle(kps,LEFT_SHOULDER),get_xy_for_angle(kps,LEFT_HIP),get_xy_for_angle(kps,LEFT_ANKLE)),
        calc_angle(get_xy_for_angle(kps,RIGHT_SHOULDER),get_xy_for_angle(kps,RIGHT_HIP),get_xy_for_angle(kps,RIGHT_ANKLE)),
        calc_angle(get_xy_for_angle(kps,LEFT_SHOULDER),get_xy_for_angle(kps,LEFT_ELBOW),get_xy_for_angle(kps,LEFT_WRIST)),
        calc_angle(get_xy_for_angle(kps,RIGHT_SHOULDER),get_xy_for_angle(kps,RIGHT_ELBOW),get_xy_for_angle(kps,RIGHT_WRIST)),
        calc_angle(get_xy_for_angle(kps,LEFT_HIP),get_xy_for_angle(kps,LEFT_SHOULDER),get_xy_for_angle(kps,LEFT_ELBOW)),
        calc_angle(get_xy_for_angle(kps,RIGHT_HIP),get_xy_for_angle(kps,RIGHT_SHOULDER),get_xy_for_angle(kps,RIGHT_ELBOW)),
        calc_angle(get_xy_for_angle(kps,LEFT_EAR),get_xy_for_angle(kps,LEFT_SHOULDER),get_xy_for_angle(kps,LEFT_HIP)),
        calc_angle(get_xy_for_angle(kps,RIGHT_EAR),get_xy_for_angle(kps,RIGHT_SHOULDER),get_xy_for_angle(kps,RIGHT_HIP)),
        calc_angle(get_xy_for_angle(kps,LEFT_HIP),get_xy_for_angle(kps,LEFT_KNEE),get_xy_for_angle(kps,LEFT_ANKLE)),
        calc_angle(get_xy_for_angle(kps,RIGHT_HIP),get_xy_for_angle(kps,RIGHT_KNEE),get_xy_for_angle(kps,RIGHT_ANKLE)),
        abs(get_xyz(kps,LEFT_HIP)[1]-get_xyz(kps,RIGHT_HIP)[1])*100,
        abs(get_xyz(kps,LEFT_SHOULDER)[1]-get_xyz(kps,RIGHT_SHOULDER)[1])*100,
    ]
    full = norm + angles  
    scaled_features = scaler.transform([full])[0]
    return scaled_features, angles # Trả về cả 2

# --- APP CHÍNH ---
class PlankTesterApp:
    def __init__(self, window, window_title):
        self.window = window
        self.window.title(window_title)
        
        self.model_path = 'pose_correction_exercise_aware.tflite'
        self.json_path = 'label_mappings.json'
        self.scaler_path = 'feature_scaler.pkl' # BẮT BUỘC PHẢI CÓ FILE NÀY
        
        if not os.path.exists(self.model_path) or not os.path.exists(self.scaler_path):
            print("❌ LỖI: Thiếu file model hoặc scaler!")
            exit()
            
        with open(self.json_path, 'r', encoding='utf-8') as f:
            self.mappings = json.load(f)
            
        with open(self.scaler_path, 'rb') as f:
            self.scaler = pickle.load(f)
            
        self.interpreter = tf.lite.Interpreter(model_path=self.model_path)
        self.interpreter.allocate_tensors()
        self.input_details = self.interpreter.get_input_details()
        self.output_details = self.interpreter.get_output_details()
        
        self.kp_input_idx = next(i['index'] for i in self.input_details if i['shape'][1] == 144)
        self.ex_input_idx = next(i['index'] for i in self.input_details if i['shape'][1] == 8)
        
        self.mp_pose = mp.solutions.pose
        self.pose = self.mp_pose.Pose(min_detection_confidence=0.5, min_tracking_confidence=0.5)
        self.mp_drawing = mp.solutions.drawing_utils
        
        self.video_label = tk.Label(window)
        self.video_label.pack(padx=10, pady=10)
        
        btn_frame = tk.Frame(window)
        btn_frame.pack(fill=tk.X, pady=10)
        self.btn_start = tk.Button(btn_frame, text="Bắt Đầu Test", width=20, bg='green', fg='white', font=('Arial', 12, 'bold'), command=self.toggle_test)
        self.btn_start.pack(side=tk.LEFT, padx=50)
        
        self.is_testing = False
        self.cap = cv2.VideoCapture(0)
        self.update_frame()
        self.window.mainloop()

    def toggle_test(self):
        self.is_testing = not self.is_testing
        if self.is_testing: self.btn_start.config(text="Đang Test...", bg='orange')
        else: self.btn_start.config(text="Bắt Đầu Test", bg='green')

    def predict(self, results):
        if not results.pose_landmarks: return {}
        
        raw_kps = []
        for lm in results.pose_landmarks.landmark:
            raw_kps.extend([lm.x, lm.y, lm.z, lm.visibility])
            
        # Lấy cả 2
        features, raw_angles = preprocess_for_inference(raw_kps, self.scaler)
        features = np.expand_dims(features, axis=0).astype(np.float32)
        
        exercise_dummy = np.zeros((1, 8), dtype=np.float32)
        exercise_dummy[0, 0] = 1.0 
        
        self.interpreter.set_tensor(self.kp_input_idx, features)
        self.interpreter.set_tensor(self.ex_input_idx, exercise_dummy)
        self.interpreter.invoke()
        
        preds = {}
        model_body_part = "none"
        
        for out in self.output_details:
            name, tensor, shape = out['name'].lower(), self.interpreter.get_tensor(out['index'])[0], out['shape']
            if 'body_part' in name or shape[1] == len(self.mappings['body_part']):
                idx = np.argmax(tensor)
                model_body_part = self.mappings['body_part'].get(str(idx), "unknown")
                preds['prob'] = tensor[idx] 

        # ÁP DỤNG RULE-BASED LAYER ĐỂ OVERRIDE
        final_body_part = rule_based_override(raw_angles, model_body_part)
        preds['body_part'] = final_body_part
                
        if final_body_part == 'none':
            preds['label'] = "CHUAN" 
        else:
            preds['label'] = "SAI"
            
        return preds

    def update_frame(self):
        ret, frame = self.cap.read()
        if ret:
            frame = cv2.flip(frame, 1)
            if self.is_testing:
                rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                results = self.pose.process(rgb_frame)
                
                if results.pose_landmarks:
                    self.mp_drawing.draw_landmarks(frame, results.pose_landmarks, self.mp_pose.POSE_CONNECTIONS)
                    pred = self.predict(results)
                    
                    lbl = pred.get('label', '')
                    color = (0, 255, 0) if lbl == "CHUAN" else (0, 0, 255)
                    
                    # HIỂN THỊ STATUS CHÍNH
                    cv2.putText(frame, f"Status: {lbl}", (10, 40), cv2.FONT_HERSHEY_SIMPLEX, 1, color, 2)
                    
                    # HIỂN THỊ CHI TIẾT
                    prob = pred.get('prob', 0) * 100
                    if lbl == "SAI":
                        error_msg = f"Error: {pred.get('body_part')} ({prob:.1f}%)"
                        cv2.putText(frame, error_msg, (10, 80), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 2)
                    elif lbl == "CHUAN":
                        success_msg = f"Perfect Plank! ({prob:.1f}%)"
                        cv2.putText(frame, success_msg, (10, 80), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 2)

            img = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
            self.photo = ImageTk.PhotoImage(image=img)
            self.video_label.config(image=self.photo)
            
        self.window.after(15, self.update_frame)

    

if __name__ == "__main__":
    root = tk.Tk()
    app = PlankTesterApp(root, "Plank Tester PRO")