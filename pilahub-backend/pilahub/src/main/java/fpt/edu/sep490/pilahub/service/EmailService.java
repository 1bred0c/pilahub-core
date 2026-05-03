package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.pojo.LiveSessionReport;

public interface EmailService {

    void sendOtpEmail(String toEmail, String otpCode);

    void sendWelcomeEmail(String toEmail, String name);

    void sendPasswordResetOtp(String toEmail, String otpCode);

    void sendPasswordChangedNotification(String toEmail);

    void sendReportCreatedNotificationToReporter(String toEmail, String name, LiveSessionReport report);

    void sendReportCreatedNotificationToReportedUser(String toEmail, String name, LiveSessionReport report);
}
