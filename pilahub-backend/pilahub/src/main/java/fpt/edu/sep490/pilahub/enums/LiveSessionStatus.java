package fpt.edu.sep490.pilahub.enums;

public enum LiveSessionStatus {
    PENDING,    // Token chưa gen, chưa có session nào được tạo

    ACTIVE,     // Token đã gen, đang chờ hoặc đang diễn ra session
    // Bao gồm: chờ join, 1 người join, cả 2 join, đang học

    COMPLETED,  // Session kết thúc bình thường (endTime đến hoặc manual end)
    // Recording (nếu có) đã được trigger stop

    NO_SHOW,    // Quá 15 phút không ai join hoặc chỉ 1 người join
    // Session tự động hủy

    FAILED      // Có lỗi technical (Agora API fail, token gen fail...)
}
