# 📧 Gmail SMTP Configuration Guide

## Overview
Hệ thống đã được cấu hình để gửi email thực tế qua Gmail SMTP. Các credentials bí mật được lưu trữ an toàn trong `application-dev.properties`.

---

## 🔐 Security Best Practices

### ❌ Không Bao Giờ Để
- ❌ Real Gmail password trực tiếp trong code
- ❌ Credentials trong `application.properties` (public)
- ❌ Push credentials lên Git repository
- ❌ Hard-code email/password trong Java files

### ✅ Cách Bảo Mật Đúng
- ✅ Dùng **Gmail App Password** (không phải mật khẩu tài khoản)
- ✅ Lưu trong `application-dev.properties` (gitignored)
- ✅ Dùng environment variables cho production
- ✅ Keep credentials trong `.gitignore`

---

## 🔧 Setup Hướng Dẫn

### Step 1: Bật 2-Factor Authentication trên Gmail
1. Vào https://myaccount.google.com/security
2. Enable "2-Step Verification"

### Step 2: Tạo Gmail App Password
1. Vào https://myaccount.google.com/apppasswords
2. Chọn:
   - **App**: Mail
   - **Device**: Windows Computer (hoặc device của bạn)
3. Click **Generate**
4. Copy password được hiển thị (16 ký tự)

### Step 3: Cấu Hình `application-dev.properties`

Thay thế trong file `src/main/resources/application-dev.properties`:

```properties
# Email Configuration (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=xxxx xxxx xxxx xxxx
```

**Ví dụ:**
```properties
spring.mail.username=myuniversity@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

### Step 4: Cấu Hình Sender Email
```properties
app.email.from=Ký Túc Xá Sinh Viên <myuniversity@gmail.com>
```

---

## 🚀 Test Email Gửi

### Cách 1: Qua API
```bash
POST http://localhost:8080/api/v1/auth/forgot-password
Content-Type: application/json

{
  "email": "student@university.edu.vn"
}
```

Kiểm tra logs:
```
INFO  EmailServiceImpl - HTML email sent successfully to: student@university.edu.vn
```

### Cách 2: Xem Console Logs
```
INFO ... HTML email sent successfully to: user@example.com
INFO ... Reset link sent: http://localhost:8080/reset-password.html?token=UUID
```

---

## 📋 Current Configuration

### Backend Dependencies (pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

### Services
- **EmailService**: Interface định nghĩa email operations
- **EmailServiceImpl**: Triển khai gửi email qua JavaMailSender
- **AuthServiceImpl**: Gọi EmailService khi forgot password

### Properties (application-dev.properties)
```properties
# Email Configuration (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${GMAIL_USER}
spring.mail.password=${GMAIL_APP_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Email sender
app.email.from=Ký Túc Xá Sinh Viên <dormitory@example.com>

# Password reset config
app.jwt.token-expiry-hours=24
app.frontend.url=http://localhost:8080
```

---

## 🐛 Troubleshooting

### ❌ Error: "Authentication failed"
**Nguyên nhân**: App password không đúng hoặc 2FA chưa bật
**Giải pháp**:
1. Kiểm tra lại Gmail App Password (16 ký tự, có spaces)
2. Đảm bảo 2-Step Verification đã bật
3. Thử tạo App Password mới

### ❌ Error: "Connection refused"
**Nguyên nhân**: Port 587 bị block hoặc cấu hình host sai
**Giải pháp**:
1. Kiểm tra: `spring.mail.host=smtp.gmail.com`
2. Kiểm tra: `spring.mail.port=587` (STARTTLS)
3. Nếu 587 bị block, thử port 465 (SSL)

### ❌ Error: "Less secure app access"
**Nguyên nhân**: Gmail chặn app-specific passwords
**Giải pháp**:
1. Dùng Gmail App Password (không phải regular password)
2. Enable 2-Step Verification

---

## 📧 Email Template

Email được gửi với:
- **Format**: HTML + UTF-8 encoding
- **Styling**: Inline CSS (tương thích tất cả email clients)
- **Content**:
  - Header với logo gradient (primary color)
  - Reset password link
  - Warning về security
  - Footer với copyright

**Màu sắc theo Design System**:
- Primary (#0EA5A5)
- Warning (#fff3cd)
- Border (#E2E8F0)

---

## 🔒 Production Setup

Để deploy lên production:

1. **Environment Variables**:
```bash
export GMAIL_USER=production-email@gmail.com
export GMAIL_APP_PASSWORD=xxxx xxxx xxxx xxxx
```

2. **Application.properties**:
```properties
spring.mail.username=${GMAIL_USER:default@gmail.com}
spring.mail.password=${GMAIL_APP_PASSWORD:}
app.frontend.url=${APP_FRONTEND_URL:https://yourdomain.com}
```

3. **Security Considerations**:
   - Không log credentials
   - Dùng secrets manager (AWS Secrets, Azure Key Vault, etc.)
   - Rotate app passwords định kỳ
   - Monitor email sending logs

---

## 📝 File Locations

- **EmailService.java**: `/service/EmailService.java`
- **EmailServiceImpl.java**: `/service/impl/EmailServiceImpl.java`
- **AuthServiceImpl.java**: `/service/impl/AuthServiceImpl.java` (calls EmailService)
- **AuthController.java**: `/controller/AuthController.java` (endpoints)
- **Config**: `application-dev.properties`
- **HTML Email**: Inline HTML trong EmailServiceImpl

---

## ✅ Verification Checklist

- [x] spring-boot-starter-mail dependency thêm vào pom.xml
- [x] EmailService interface tạo
- [x] EmailServiceImpl với JavaMailSender triển khai
- [x] AuthService.forgotPassword() gọi EmailService
- [x] AuthController endpoint POST /forgot-password & /reset-password
- [x] application-dev.properties cấu hình Gmail SMTP
- [x] HTML email template đẹp với styling
- [x] Error handling trong gửi email
- [x] Logging cho audit trail
- [x] Maven compile successfully ✓

---

Generated: 2026-04-03
Last Updated: Setup hệ thống email thực sự với Gmail SMTP
