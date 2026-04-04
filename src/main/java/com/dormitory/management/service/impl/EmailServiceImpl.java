package com.dormitory.management.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.dormitory.management.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${app.email.from:Ký Túc Xá Sinh Viên <dormitory@example.com>}")
    private String fromEmail;

    @Override
    public void sendResetPasswordEmail(String email, String resetLink) {
        String subject = "🔐 Đặt lại mật khẩu - Ký túc xá sinh viên";
        String htmlContent = buildResetPasswordEmailHtml(resetLink);
        sendHtmlEmail(email, subject, htmlContent);
    }

    @Override
    public void sendNotificationEmail(String email, String subject, String message) {
        sendSimpleEmail(email, subject, message);
    }

    private void sendSimpleEmail(String toEmail, String subject, String body) {
        if (javaMailSender == null) {
            LOGGER.warn("JavaMailSender is not configured. Skipping simple email to: {} with subject: {}", toEmail, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);
            LOGGER.info("Simple email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            LOGGER.error("Failed to send simple email to: {}", toEmail, e);
        }
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        if (javaMailSender == null) {
            LOGGER.warn("JavaMailSender is not configured. Skipping HTML email to: {} with subject: {}", toEmail, subject);
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content

            javaMailSender.send(message);
            LOGGER.info("HTML email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            LOGGER.error("Failed to send HTML email to: {}", toEmail, e);
        }
    }

    private String buildResetPasswordEmailHtml(String resetLink) {
        return "<!DOCTYPE html>\n"
                + "<html lang=\"vi\">\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\">\n"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    <style>\n"
                + "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }\n"
                + "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n"
                + "        .header { background: linear-gradient(135deg, #0EA5A5 0%, #0B8F8F 100%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }\n"
                + "        .header h1 { margin: 0; font-size: 24px; }\n"
                + "        .content { background: #f9f9f9; padding: 30px; border: 1px solid #e0e0e0; }\n"
                + "        .content p { margin: 15px 0; }\n"
                + "        .reset-btn { display: inline-block; background: #0EA5A5; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 20px 0; }\n"
                + "        .reset-btn:hover { background: #0B8F8F; }\n"
                + "        .footer { background: #f0f0f0; padding: 20px; text-align: center; font-size: 12px; color: #666; border: 1px solid #e0e0e0; border-radius: 0 0 8px 8px; }\n"
                + "        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 12px; margin: 15px 0; border-radius: 4px; }\n"
                + "        .reset-link { word-break: break-all; padding: 10px; background: white; border: 1px solid #ddd; border-radius: 4px; font-family: monospace; font-size: 12px; }\n"
                + "    </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "    <div class=\"container\">\n"
                + "        <div class=\"header\">\n"
                + "            <h1>🔐 Đặt lại mật khẩu</h1>\n"
                + "        </div>\n"
                + "        <div class=\"content\">\n"
                + "            <p>Xin chào,</p>\n"
                + "            <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình tại <strong>Ký túc xá sinh viên</strong>.</p>\n"
                + "            <p>Vui lòng nhấp vào nút dưới đây để đặt lại mật khẩu:</p>\n"
                + "            <center><a href=\"" + resetLink + "\" class=\"reset-btn\">Đặt lại mật khẩu</a></center>\n"
                + "            <p style=\"text-align: center; color: #666; font-size: 14px;\">hoặc sao chép liên kết dưới đây:</p>\n"
                + "            <div class=\"reset-link\">" + resetLink + "</div>\n"
                + "            <div class=\"warning\">\n"
                + "                <strong>⚠️ Lưu ý:</strong><br/>\n"
                + "                • Liên kết sẽ hết hạn sau <strong>1 giờ</strong><br/>\n"
                + "                • Không chia sẻ liên kết này với ai khác<br/>\n"
                + "                • Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này\n"
                + "            </div>\n"
                + "            <p>Nếu có bất kỳ vấn đề nào, vui lòng liên hệ với đội hỗ trợ.</p>\n"
                + "        </div>\n"
                + "        <div class=\"footer\">\n"
                + "            <p>&copy; 2026 Ký túc xá sinh viên. All rights reserved.</p>\n"
                + "            <p>Đây là email tự động, vui lòng không trả lời email này.</p>\n"
                + "        </div>\n"
                + "    </div>\n"
                + "</body>\n"
                + "</html>";
    }
}