package com.dormitory.management.service.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.dormitory.management.service.EmailService;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${app.email.from:Ký Túc Xá Sinh Viên <dormitory@example.com>}")
    private String fromEmail;

    @Value("${app.email.smtp-fallback-enabled:true}")
    private boolean smtpFallbackEnabled;

    @Value("${app.email.smtp-fallback-host:smtp-relay.brevo.com}")
    private String smtpFallbackHost;

    @Value("${app.email.smtp-fallback-port:465}")
    private int smtpFallbackPort;

    @Value("${spring.mail.properties.mail.smtp.connectiontimeout:20000}")
    private String smtpConnectionTimeout;

    @Value("${spring.mail.properties.mail.smtp.timeout:20000}")
    private String smtpTimeout;

    @Value("${spring.mail.properties.mail.smtp.writetimeout:20000}")
    private String smtpWriteTimeout;

    @Value("${app.email.brevo-api-fallback-enabled:true}")
    private boolean brevoApiFallbackEnabled;

    @Value("${app.email.brevo-api-key:}")
    private String brevoApiKey;

    @Value("${app.email.brevo-api-url:https://api.brevo.com/v3/smtp/email}")
    private String brevoApiUrl;

    @Override
    public boolean sendResetPasswordEmail(String email, String resetLink) {
        String subject = "🔐 Đặt lại mật khẩu - Ký túc xá sinh viên";
        String htmlContent = buildResetPasswordEmailHtml(resetLink);
        return sendHtmlEmail(email, subject, htmlContent);
    }

    @Override
    public boolean sendNotificationEmail(String email, String subject, String message) {
        return sendSimpleEmail(email, subject, message);
    }

    private boolean sendSimpleEmail(String toEmail, String subject, String body) {
        if (javaMailSender == null) {
            LOGGER.warn("JavaMailSender is not configured. Skipping simple email to: {} with subject: {}", toEmail, subject);
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);
            LOGGER.info("Simple email sent successfully to: {}", toEmail);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to send simple email to: {}", toEmail, e);
            return false;
        }
    }

    private boolean sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        if (javaMailSender == null) {
            LOGGER.warn("JavaMailSender is not configured. Skipping HTML email to: {} with subject: {}", toEmail, subject);
            return sendHtmlEmailViaBrevoApi(toEmail, subject, htmlContent, null);
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
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to send HTML email to: {}", toEmail, e);
            boolean smtpFallbackSent = sendHtmlEmailViaFallback(toEmail, subject, htmlContent, e);
            if (smtpFallbackSent) {
                return true;
            }
            return sendHtmlEmailViaBrevoApi(toEmail, subject, htmlContent, e);
        }
    }

    private boolean sendHtmlEmailViaFallback(String toEmail, String subject, String htmlContent, Exception primaryException) {
        if (!smtpFallbackEnabled) {
            return false;
        }

        if (!(javaMailSender instanceof JavaMailSenderImpl primarySender)) {
            return false;
        }

        try {
            JavaMailSenderImpl fallbackSender = new JavaMailSenderImpl();
            fallbackSender.setHost(smtpFallbackHost);
            fallbackSender.setPort(smtpFallbackPort);
            fallbackSender.setUsername(primarySender.getUsername());
            fallbackSender.setPassword(primarySender.getPassword());

            Properties props = fallbackSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.starttls.required", "false");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.connectiontimeout", smtpConnectionTimeout);
            props.put("mail.smtp.timeout", smtpTimeout);
            props.put("mail.smtp.writetimeout", smtpWriteTimeout);

            MimeMessage message = fallbackSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            fallbackSender.send(message);
            LOGGER.warn("Primary SMTP send failed, fallback SMTP send succeeded via {}:{} for {}", smtpFallbackHost, smtpFallbackPort, toEmail);
            return true;
        } catch (Exception fallbackException) {
            LOGGER.error("Fallback SMTP send also failed for: {}", toEmail, fallbackException);
            LOGGER.debug("Primary SMTP failure root cause:", primaryException);
            return false;
        }
    }

    private boolean sendHtmlEmailViaBrevoApi(String toEmail, String subject, String htmlContent, Exception primaryException) {
        if (!brevoApiFallbackEnabled) {
            return false;
        }

        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            LOGGER.warn("Brevo API fallback is enabled but app.email.brevo-api-key is missing.");
            return false;
        }

        String senderEmail = extractSenderEmail(fromEmail);
        if (senderEmail == null || senderEmail.isBlank()) {
            LOGGER.warn("Brevo API fallback skipped because sender email is invalid: {}", fromEmail);
            return false;
        }

        String senderName = extractSenderName(fromEmail);

        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();

            String payload = "{" +
                    "\"sender\":{" +
                    "\"name\":\"" + jsonEscape(senderName) + "\"," +
                    "\"email\":\"" + jsonEscape(senderEmail) + "\"}," +
                    "\"to\":[{\"email\":\"" + jsonEscape(toEmail) + "\"}]," +
                    "\"subject\":\"" + jsonEscape(subject) + "\"," +
                    "\"htmlContent\":\"" + jsonEscape(htmlContent) + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(brevoApiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("api-key", brevoApiKey.trim())
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                LOGGER.warn("SMTP send failed but Brevo API fallback succeeded for: {}", toEmail);
                return true;
            }

            LOGGER.error("Brevo API fallback failed with status {} for {}. Response: {}", statusCode, toEmail, response.body());
            if (primaryException != null) {
                LOGGER.debug("Primary SMTP failure root cause:", primaryException);
            }
            return false;
        } catch (Exception ex) {
            LOGGER.error("Brevo API fallback request failed for: {}", toEmail, ex);
            if (primaryException != null) {
                LOGGER.debug("Primary SMTP failure root cause:", primaryException);
            }
            return false;
        }
    }

    private String extractSenderEmail(String sender) {
        if (sender == null) {
            return "";
        }

        String text = sender.trim();
        int left = text.indexOf('<');
        int right = text.indexOf('>');
        if (left >= 0 && right > left) {
            return text.substring(left + 1, right).trim();
        }
        return text;
    }

    private String extractSenderName(String sender) {
        if (sender == null) {
            return "KTX Sinh Vien";
        }

        String text = sender.trim();
        int left = text.indexOf('<');
        if (left > 0) {
            return text.substring(0, left).trim();
        }
        return "KTX Sinh Vien";
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
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