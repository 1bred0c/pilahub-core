package fpt.edu.sep490.pilahub.enums;

public enum NotificationType {

    // General
    SYSTEM,

    // Coach booking lifecycle
    BOOKING_CONFIRMED,          // new booking created – both parties
    BOOKING_CANCELLED,          // cancelled by coach or trainee
    BOOKING_REFUNDED,           // trainee refunded after coach cancels / no-show
    BOOKING_IN_PROGRESS,        // session started (one party joined)
    BOOKING_COMPLETED,          // session fully completed

    // Course & lesson progress
    COURSE_ENROLLED,            // trainee enrolled in a course
    COURSE_COMPLETED,           // trainee completed all lessons
    LESSON_COMPLETED,           // single lesson marked done

    // Wallet – deposits (VNPay)
    WALLET_DEPOSIT_SUCCESS,
    WALLET_DEPOSIT_FAILED,

    // Wallet – withdrawals
    WALLET_WITHDRAWAL_REQUESTED,
    WALLET_WITHDRAWAL_APPROVED,
    WALLET_WITHDRAWAL_REJECTED,
    WALLET_WITHDRAWAL_COMPLETED,

    // Coach feedback
    COACH_FEEDBACK_RECEIVED,

    // Post interaction
    POST_REACTED,
    POST_COMMENTED,
    POST_COMMENT_REPLIED,

    // Account management
    ACCOUNT_DEACTIVATED,
    ACCOUNT_REACTIVATED,

    // Health assessment (AI)
    HEALTH_ASSESSMENT_READY,

    ORDER
}
