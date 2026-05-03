package fpt.edu.sep490.pilahub.enums;

public enum  BookingStatus {
    SCHEDULED, // Khi người dùng book lịch, lịch sẽ ở trạng thái SCHEDULED, có nghĩa là đã được lên lịch nhưng chưa diễn ra
    CANCELLED_BY_COACH, //Trạng thái khi Coach hủy lịch khi booking đang ở trạng thái SCHEDULED, có nghĩa là Coach đã hủy lịch đã được lên lịch nhưng chưa diễn ra
    CANCELLED_BY_TRAINEE, //Trạng thái khi Trainee hủy lịch khi booking đang ở trạng thái SCHEDULED, có nghĩa là Trainee đã hủy lịch đã được
    READY, // Trạng thái khi booking đã đến giờ bắt đầu, Trainee và Coach có thể join để cùng nhau học tập, chỉ khi chuyển qua trạng thái này thì mới có thể join được, nếu chưa đến giờ bắt đầu thì sẽ không thể join được
    IN_PROGRESS, // Trạng thái khi booking đã bắt đầu, có đủ điều kiện là Trainee và Coach đều đã tham gia vào phòng học, có nghĩa là cả hai đã join vào phòng học và đã đến giờ bắt đầu, khi đó trạng thái sẽ chuyển từ READY sang IN_PROGRESS
    NO_SHOW_BY_COACH, // Trạng thái khi Coach không tham gia vào phòng học khi booking đã đến giờ bắt đầu, có nghĩa là Coach đã không join vào phòng học khi booking đã đến giờ bắt đầu được 15 phút, khi đó trạng thái sẽ chuyển từ READY sang NO_SHOW_BY_COACH
    NO_SHOW_BY_TRAINEE, // Trạng thái khi Trainee không tham gia vào phòng học khi booking đã đến giờ bắt đầu, có nghĩa là Trainee đã không join vào phòng học khi booking đã đến giờ bắt đầu được 15 phút, khi đó trạng thái sẽ chuyển từ READY sang NO_SHOW_BY_TRAINEE
    REFUNDED, // Trạng thái này sẽ được sử dụng trong 2 trường hợp: 1 là Coach cancelled lịch khi boooking đang ở trạng thái SCHEDULED, có nghĩa là Coach đã hủy lịch đã được lên lịch nhưng chưa diễn ra, khi đó trạng thái sẽ chuyển từ CANCELLED_BY_COACH sang REFUNDED, 2 là khi Coach no show, sẽ chuyển từ NO_SHOW_BY_COACH sang REFUNDED, có nghĩa là Coach đã không join vào phòng học khi booking đã đến giờ bắt đầu được 15 phút, khi đó trạng thái sẽ chuyển từ NO_SHOW_BY_COACH sang REFUNDED, và khi đó hệ thống sẽ tự động refund tiền cho Trainee
    COMPLETED // Trạng thái khi booking đã hoàn thành, có nghĩa là cả Coach và Trainee đều đã tham gia vào phòng học và đã kết thúc buổi học, khi đó trạng thái sẽ chuyển từ IN_PROGRESS sang COMPLETED

   // Tóm lại, có 4 trạng thái dẫn đến nút END gồm: CANCELLED_BY_TRAINEE, REFUNDED, NO_SHOW_BY_TRAINEE, COMPLETED, khi booking chuyển sang 1 trong 4 trạng thái này thì sẽ không thể join được nữa, và booking sẽ được coi là đã kết thúc, không còn hiệu lực nữa
    // Còn trường hợp CANCELLED_BY_COACH và NO_SHOW_BY_COACH sẽ dẫn đến nút REFUNDED, khi booking chuyển sang trạng thái REFUNDED thì sẽ không thể join được nữa, và booking sẽ được coi là đã kết thúc, không còn hiệu lực nữa
}
