package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.pojo.LiveSessionReport;
import fpt.edu.sep490.pilahub.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@pilahub.com}")
    private String fromEmail;

    @Override
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PilaHub - Email Verification Code");
            helper.setText(buildOtpEmailContent(otpCode), true);
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to PilaHub!");
            helper.setText(buildWelcomeEmailContent(name), true);
            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetOtp(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PilaHub - Password Reset Code");
            helper.setText(buildPasswordResetEmailContent(otpCode), true);

            mailSender.send(message);
            log.info("Password reset OTP email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}. Error: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendPasswordChangedNotification(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PilaHub - Password Changed Successfully");
            helper.setText(buildPasswordChangedEmailContent(), true);

            mailSender.send(message);
            log.info("Password changed notification sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password changed notification to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendReportCreatedNotificationToReporter(String toEmail, String name, LiveSessionReport report) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PilaHub - Báo cáo phiên học của bạn đã được ghi nhận");
            helper.setText(buildReportCreatedEmailContentForReporter(name, report), true);
            mailSender.send(message);
            log.info("Report notification email sent to reporter: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send report notification to reporter: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendReportCreatedNotificationToReportedUser(String toEmail, String name, LiveSessionReport report) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PilaHub - Bạn đã nhận được một báo cáo từ học viên");
            helper.setText(buildReportCreatedEmailContentForReportedUser(name, report), true);
            mailSender.send(message);
            log.info("Report notification email sent to reported user: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send report notification to reported user: {}", toEmail, e);
        }
    }

    private String buildOtpEmailContent(String otpCode) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Email Verification - PilaHub</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f4f7fa;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse; background-color: #f4f7fa;">
                        <tr>
                            <td align="center" style="padding: 40px 20px;">
                                <table role="presentation" style="width: 100%%; max-width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);">

                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 40px 40px 30px; text-align: center; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); border-radius: 12px 12px 0 0;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">PilaHub</h1>
                                            <p style="margin: 10px 0 0; color: rgba(255, 255, 255, 0.9); font-size: 16px; font-weight: 400;">Email Verification</p>
                                        </td>
                                    </tr>

                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="margin: 0 0 20px; color: #1a202c; font-size: 24px; font-weight: 600; line-height: 1.3;">Verify Your Email Address</h2>
                                            <p style="margin: 0 0 25px; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                Thank you for registering with PilaHub. To complete your registration and verify your email address, please use the verification code below:
                                            </p>

                                            <!-- OTP Box -->
                                            <table role="presentation" style="width: 100%%; border-collapse: collapse; margin: 30px 0;">
                                                <tr>
                                                    <td align="center" style="padding: 20px; background-color: #f7fafc; border-radius: 8px; border: 2px dashed #cbd5e0;">
                                                        <div style="font-size: 36px; font-weight: 700; color: #667eea; letter-spacing: 8px; font-family: 'Courier New', monospace;">%s</div>
                                                        <p style="margin: 12px 0 0; color: #718096; font-size: 13px; font-weight: 500;">VERIFICATION CODE</p>
                                                    </td>
                                                </tr>
                                            </table>

                                            <!-- Information Box -->
                                            <div style="background-color: #fff5f5; border-left: 4px solid #fc8181; padding: 16px 20px; border-radius: 4px; margin: 25px 0;">
                                                <p style="margin: 0; color: #742a2a; font-size: 14px; line-height: 1.5;">
                                                    <strong>Important:</strong> This verification code will expire in 5 minutes. Please do not share this code with anyone.
                                                </p>
                                            </div>

                                            <p style="margin: 25px 0 0; color: #4a5568; font-size: 15px; line-height: 1.6;">
                                                If you did not create an account with PilaHub, please disregard this email and no further action is required.
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 30px 40px; background-color: #f7fafc; border-radius: 0 0 12px 12px; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 10px; color: #718096; font-size: 14px; line-height: 1.5;">
                                                Best regards,<br>
                                                <strong style="color: #4a5568;">The PilaHub Team</strong>
                                            </p>
                                            <p style="margin: 15px 0 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                                This is an automated message, please do not reply to this email.
                                            </p>
                                        </td>
                                    </tr>

                                </table>

                                <!-- Footer Note -->
                                <p style="margin: 20px 0 0; color: #a0aec0; font-size: 12px; text-align: center; line-height: 1.5;">
                                    &copy; 2026 PilaHub. All rights reserved.
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, otpCode);
    }

    private String buildWelcomeEmailContent(String name) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Welcome to PilaHub</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f4f7fa;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse; background-color: #f4f7fa;">
                        <tr>
                            <td align="center" style="padding: 40px 20px;">
                                <table role="presentation" style="width: 100%%; max-width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);">

                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 40px 40px 30px; text-align: center; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); border-radius: 12px 12px 0 0;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">PilaHub</h1>
                                            <p style="margin: 10px 0 0; color: rgba(255, 255, 255, 0.9); font-size: 16px; font-weight: 400;">Welcome Aboard</p>
                                        </td>
                                    </tr>

                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="margin: 0 0 20px; color: #1a202c; font-size: 24px; font-weight: 600; line-height: 1.3;">Welcome to PilaHub, %s!</h2>
                                            <p style="margin: 0 0 20px; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                Congratulations! Your email has been successfully verified and your account is now active.
                                            </p>
                                            <p style="margin: 0 0 25px; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                You can now access all the features and services that PilaHub has to offer. We're excited to have you as part of our community!
                                            </p>

                                            <!-- Success Icon -->
                                            <table role="presentation" style="width: 100%%; border-collapse: collapse; margin: 30px 0;">
                                                <tr>
                                                    <td align="center" style="padding: 30px; background-color: #f0fff4; border-radius: 8px; border: 2px solid #9ae6b4;">
                                                        <div style="width: 64px; height: 64px; margin: 0 auto 15px; background-color: #48bb78; border-radius: 50%%; display: flex; align-items: center; justify-content: center;">
                                                            <div style="width: 0; height: 0; border-left: 12px solid transparent; border-right: 12px solid transparent; border-bottom: 20px solid #ffffff; transform: rotate(45deg); margin-left: -4px; margin-top: 8px;"></div>
                                                        </div>
                                                        <p style="margin: 0; color: #22543d; font-size: 18px; font-weight: 600;">Account Activated Successfully</p>
                                                    </td>
                                                </tr>
                                            </table>

                                            <p style="margin: 25px 0 0; color: #4a5568; font-size: 15px; line-height: 1.6;">
                                                If you have any questions or need assistance, please don't hesitate to contact our support team.
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 30px 40px; background-color: #f7fafc; border-radius: 0 0 12px 12px; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 10px; color: #718096; font-size: 14px; line-height: 1.5;">
                                                Best regards,<br>
                                                <strong style="color: #4a5568;">The PilaHub Team</strong>
                                            </p>
                                            <p style="margin: 15px 0 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                                This is an automated message, please do not reply to this email.
                                            </p>
                                        </td>
                                    </tr>

                                </table>

                                <!-- Footer Note -->
                                <p style="margin: 20px 0 0; color: #a0aec0; font-size: 12px; text-align: center; line-height: 1.5;">
                                    &copy; 2026 PilaHub. All rights reserved.
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, name);
    }

    private String buildPasswordResetEmailContent(String otpCode) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Reset - PilaHub</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f4f7fa;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse; background-color: #f4f7fa;">
                        <tr>
                            <td align="center" style="padding: 40px 20px;">
                                <table role="presentation" style="width: 100%%; max-width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);">
                                    <tr>
                                        <td style="padding: 40px 40px 30px; text-align: center; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); border-radius: 12px 12px 0 0;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700;">PilaHub</h1>
                                            <p style="margin: 10px 0 0; color: rgba(255, 255, 255, 0.9); font-size: 16px;">Password Reset Request</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="margin: 0 0 20px; color: #1a202c; font-size: 24px; font-weight: 600;">Reset Your Password</h2>
                                            <p style="margin: 0 0 25px; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                We received a request to reset your password. Use the code below to set a new password for your account:
                                            </p>
                                            <table role="presentation" style="width: 100%%; border-collapse: collapse; margin: 30px 0;">
                                                <tr>
                                                    <td align="center" style="padding: 20px; background-color: #f7fafc; border-radius: 8px; border: 2px dashed #cbd5e0;">
                                                        <div style="font-size: 36px; font-weight: 700; color: #667eea; letter-spacing: 8px; font-family: 'Courier New', monospace;">%s</div>
                                                        <p style="margin: 12px 0 0; color: #718096; font-size: 13px; font-weight: 500;">RESET CODE</p>
                                                    </td>
                                                </tr>
                                            </table>
                                            <div style="background-color: #fff5f5; border-left: 4px solid #fc8181; padding: 16px 20px; border-radius: 4px; margin: 25px 0;">
                                                <p style="margin: 0; color: #742a2a; font-size: 14px; line-height: 1.5;">
                                                    <strong>Security Notice:</strong> This code expires in 5 minutes. If you didn't request a password reset, please ignore this email and ensure your account is secure.
                                                </p>
                                            </div>
                                            <p style="margin: 25px 0 0; color: #4a5568; font-size: 15px; line-height: 1.6;">
                                                For security reasons, never share this code with anyone, including PilaHub staff.
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 30px 40px; background-color: #f7fafc; border-radius: 0 0 12px 12px; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 10px; color: #718096; font-size: 14px; line-height: 1.5;">
                                                Best regards,<br>
                                                <strong style="color: #4a5568;">The PilaHub Team</strong>
                                            </p>
                                            <p style="margin: 15px 0 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                                This is an automated message, please do not reply to this email.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                                <p style="margin: 20px 0 0; color: #a0aec0; font-size: 12px; text-align: center;">
                                    &copy; 2026 PilaHub. All rights reserved.
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, otpCode);
    }

    private String buildPasswordChangedEmailContent() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Changed - PilaHub</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f4f7fa;">
                    <table role="presentation" style="width: 100%; border-collapse: collapse; background-color: #f4f7fa;">
                        <tr>
                            <td align="center" style="padding: 40px 20px;">
                                <table role="presentation" style="width: 100%; max-width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);">
                                    <tr>
                                        <td style="padding: 40px 40px 30px; text-align: center; background: linear-gradient(135deg, #48bb78 0%, #38a169 100%); border-radius: 12px 12px 0 0;">
                                            <div style="width: 60px; height: 60px; background-color: rgba(255, 255, 255, 0.2); border-radius: 50%; margin: 0 auto 15px; display: flex; align-items: center; justify-content: center;">
                                                <div style="font-size: 32px;">✓</div>
                                            </div>
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700;">Password Changed</h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="margin: 0 0 20px; color: #1a202c; font-size: 24px; font-weight: 600;">Your Password Has Been Updated</h2>
                                            <p style="margin: 0 0 20px; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                This email confirms that your PilaHub account password has been successfully changed.
                                            </p>
                                            <div style="background-color: #f0fff4; border-left: 4px solid #48bb78; padding: 16px 20px; border-radius: 4px; margin: 25px 0;">
                                                <p style="margin: 0; color: #22543d; font-size: 14px; line-height: 1.5;">
                                                    <strong>Security Check:</strong> If you did not make this change, please contact our support team immediately to secure your account.
                                                </p>
                                            </div>
                                            <p style="margin: 25px 0 0; color: #4a5568; font-size: 15px; line-height: 1.6;">
                                                You can now use your new password to log in to your account.
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 30px 40px; background-color: #f7fafc; border-radius: 0 0 12px 12px; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 10px; color: #718096; font-size: 14px; line-height: 1.5;">
                                                Best regards,<br>
                                                <strong style="color: #4a5568;">The PilaHub Team</strong>
                                            </p>
                                            <p style="margin: 15px 0 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                                This is an automated message, please do not reply to this email.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                                <p style="margin: 20px 0 0; color: #a0aec0; font-size: 12px; text-align: center;">
                                    &copy; 2026 PilaHub. All rights reserved.
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;
    }

    private String buildReportCreatedEmailContentForReporter(String name, LiveSessionReport report) {
        String reasonDisplay = convertReasonToVietnamese(report.getReason());
        String formattedTime = report.getCreatedAt().toString();
        String description = report.getDescription() != null && !report.getDescription().isBlank() 
                    ? String.format("<p><strong>Mô tả chi tiết:</strong> %s</p>", report.getDescription()) 
                    : "";
        
        return "<html lang=\"vi\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><style>body{font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;background-color:#f5f5f5;margin:0;padding:20px}.container{max-width:600px;margin:0 auto;background-color:white;border-radius:8px;box-shadow:0 2px 4px rgba(0,0,0,0.1);overflow:hidden}.header{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:30px;text-align:center}.content{padding:30px}.info-box{background-color:#f8f9fa;border-left:4px solid #667eea;padding:15px;margin:20px 0;border-radius:4px}.footer{background-color:#f8f9fa;padding:20px;text-align:center;font-size:12px;color:#666}</style></head><body><div class=\"container\"><div class=\"header\"><h1>Báo Cáo Đã Được Ghi Nhận</h1></div><div class=\"content\"><p>Xin chào <strong>" 
                + name 
                + "</strong>,</p><p>Cảm ơn bạn đã báo cáo buổi coaching. Chúng tôi đã ghi nhận báo cáo của bạn và sẽ xem xét trong thời gian sớm nhất.</p><div class=\"info-box\"><p><strong>Thông tin báo cáo:</strong></p><p><strong>Lý do báo cáo:</strong> " 
                + reasonDisplay 
                + "</p><p><strong>Thời gian ghi nhận:</strong> " 
                + formattedTime 
                + "</p>" 
                + description 
                + "</div><p>Đội hỗ trợ của chúng tôi sẽ liên hệ lại với bạn trong vòng 24-48 giờ để xử lý báo cáo này.</p><p>Nếu bạn cần hỗ trợ thêm, vui lòng liên hệ với chúng tôi qua hệ thống hoặc email hỗ trợ.</p></div><div class=\"footer\"><p>© PilaHub. Mọi quyền được bảo lưu.</p></div></div></body></html>";
    }

    private String buildReportCreatedEmailContentForReportedUser(String name, LiveSessionReport report) {
        String reasonDisplay = convertReasonToVietnamese(report.getReason());
        String formattedTime = report.getCreatedAt().toString();
        
        return "<html lang=\"vi\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><style>body{font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;background-color:#f5f5f5;margin:0;padding:20px}.container{max-width:600px;margin:0 auto;background-color:white;border-radius:8px;box-shadow:0 2px 4px rgba(0,0,0,0.1);overflow:hidden}.header{background:linear-gradient(135deg,#f093fb 0%,#f5576c 100%);color:white;padding:30px;text-align:center}.content{padding:30px}.info-box{background-color:#f8f9fa;border-left:4px solid #f5576c;padding:15px;margin:20px 0;border-radius:4px}.footer{background-color:#f8f9fa;padding:20px;text-align:center;font-size:12px;color:#666}</style></head><body><div class=\"container\"><div class=\"header\"><h1>Thông Báo Báo Cáo Buổi Học</h1></div><div class=\"content\"><p>Xin chào <strong>" 
                + name 
                + "</strong>,</p><p>Chúng tôi thông báo rằng buổi coaching của bạn đã nhận được một báo cáo từ học viên.</p><div class=\"info-box\"><p><strong>Thông tin báo cáo:</strong></p><p><strong>Lý do báo cáo:</strong> " 
                + reasonDisplay 
                + "</p><p><strong>Thời gian ghi nhận:</strong> " 
                + formattedTime 
                + "</p></div><p>Đội hỗ trợ của chúng tôi sẽ xem xét báo cáo này và liên hệ lại với cả bạn và học viên trong vòng 24-48 giờ.</p><p>Vui lòng sẵn sàng cung cấp thêm thông tin nếu cần thiết.</p></div><div class=\"footer\"><p>© PilaHub. Mọi quyền được bảo lưu.</p></div></div></body></html>";
    }
    
    private String convertReasonToVietnamese(fpt.edu.sep490.pilahub.pojo.ReportReason reason) {
        if (reason == null) {
            return "Khac";
        }
        if (reason.getName() != null && !reason.getName().isBlank()) {
            return reason.getName();
        }
        return reason.getCode() != null ? reason.getCode() : "Khac";
    }
}



