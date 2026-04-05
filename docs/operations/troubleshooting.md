# Troubleshooting

## 1) Lỗi đăng nhập thất bại dù tài khoản tồn tại

Triệu chứng:

- API login trả 401 hoặc thông báo sai thông tin đăng nhập.

Kiểm tra:

1. Xác minh username/password đúng.
2. Kiểm tra user có `enabled=true` trong bảng `users`.
3. Kiểm tra user đã gán role trong bảng `user_roles`.
4. Kiểm tra `APP_JWT_SECRET` không bị thay đổi đột ngột giữa các phiên.

## 2) API trả 403 Forbidden

Triệu chứng:

- Có token nhưng gọi API quản trị bị chặn.

Kiểm tra:

1. Token có chứa authority tương ứng (`ROLE_ADMIN` hoặc `ROLE_STUDENT`).
2. Header Authorization đúng định dạng `Bearer <token>`.
3. Endpoint đang gọi có yêu cầu role nào theo `SecurityConfig`.

## 3) Không sinh được hóa đơn tháng

Triệu chứng:

- API generate trả về 0 hoặc log scheduler không tạo hóa đơn.

Kiểm tra:

1. Kỳ utility record đã được chốt chưa.
2. Phòng có hợp đồng `ACTIVE` trong tháng hay không.
3. Đã tồn tại hóa đơn kỳ đó (trừ trạng thái `CANCELLED`) chưa.
4. Kiểm tra pricing policy có dữ liệu hợp lệ.

## 4) Webhook PayOS không cập nhật trạng thái

Triệu chứng:

- Đã thanh toán nhưng hóa đơn không chuyển trạng thái phù hợp.

Kiểm tra:

1. URL webhook public và truy cập được từ internet.
2. Header chữ ký webhook có gửi lên hay không.
3. `APP_PAYOS_CHECKSUM_KEY` khớp với cổng thanh toán.
4. Định dạng payload và invoice code/order code map đúng bản ghi.

## 5) Lỗi gửi email quên mật khẩu

Triệu chứng:

- API forgot-password thành công logic nhưng email không tới.

Kiểm tra:

1. Cấu hình SMTP host/port/user/password.
2. Firewall hoặc policy chặn outbound SMTP.
3. Trạng thái fallback Brevo API (nếu bật).
4. Kiểm tra log exception từ `EmailServiceImpl`.

## 6) Lỗi dữ liệu ràng buộc (400)

Triệu chứng:

- Trả về thông báo vi phạm ràng buộc hoặc dữ liệu không hợp lệ.

Kiểm tra:

1. Kiểm tra unique key trong schema (`student_code`, `cccd`, `invoice_code`, ...).
2. Kiểm tra quan hệ khóa ngoại tồn tại.
3. Kiểm tra logic nghiệp vụ trạng thái trước khi chuyển bước.

## 7) Ứng dụng lên nhưng giao diện không tải đúng trang

Kiểm tra:

1. Đường dẫn HTML có tồn tại trong `src/main/resources/static`.
2. Mapping `HomeController` forward/redirect đúng route.
3. Security cho phép truy cập tài nguyên tĩnh (`/ui/**`, `/admin/**`, `/user/**`).

## 8) Đề xuất khi xử lý sự cố production

- Luôn chụp snapshot log trước khi restart.
- Không sửa dữ liệu production trực tiếp nếu chưa có backup.
- Ghi lại timeline sự cố và root cause để cập nhật tài liệu runbook.