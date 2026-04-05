# Danh Mục Biến Môi Trường

Tài liệu này tổng hợp biến cấu hình quan trọng cho hệ thống.

## 1) Nhóm ứng dụng

- `SPRING_PROFILES_ACTIVE`: profile chạy (`dev`, `prod`, `production`).
- `PORT`: cổng chạy ứng dụng.

## 2) Nhóm database

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Hệ thống cũng hỗ trợ fallback từ biến Railway/Postgres:

- `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`

## 3) Nhóm JWT

- `APP_JWT_SECRET` (mapping vào `app.jwt.secret`)
- `APP_JWT_EXPIRATION_MS` (mapping vào `app.jwt.expiration-ms`)

## 4) Nhóm email

- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTIONTIMEOUT`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_TIMEOUT`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_WRITETIMEOUT`

Biến mở rộng:

- `APP_EMAIL_FROM`
- `APP_EMAIL_SUPPORT`
- `APP_EMAIL_BREVO_API_FALLBACK_ENABLED`
- `APP_BREVO_API_KEY`
- `APP_EMAIL_BREVO_API_URL`

## 5) Nhóm frontend URL

- `APP_FRONTEND_URL` (dùng tạo link trả về và redirect trong luồng thanh toán/email).

## 6) Nhóm PayOS

- `APP_PAYOS_CLIENT_ID`
- `APP_PAYOS_API_KEY`
- `APP_PAYOS_CHECKSUM_KEY`
- `APP_PAYOS_BASE_URL`
- `APP_PAYOS_RETURN_URL`
- `APP_PAYOS_CANCEL_URL`

## 7) Nhóm scheduler

- `app.scheduler.zone`
- `app.scheduler.invoice-generation.cron`

## 8) Nhóm seed dữ liệu

- `app.seed.enabled`

Chỉ bật trong môi trường dev/demo. Không khuyến nghị bật ở production.

## 9) Nguyên tắc vận hành biến môi trường

- Tất cả secret phải lưu trong secret manager hoặc biến môi trường CI/CD.
- Không commit secret thật vào Git.
- Tách rõ bộ biến dev/staging/prod theo môi trường.
- Khi rotate secret, cần cập nhật đồng bộ service phụ thuộc (mail, payment).