# Mô Hình Dữ Liệu Nghiệp Vụ

## 1) Danh sách thực thể chính

- `Building`: tòa nhà ký túc xá.
- `RoomType`: loại phòng (sức chứa, giá cơ bản, giới tính cho phép).
- `Room`: phòng thuộc tòa nhà và loại phòng.
- `Bed`: giường thuộc phòng.
- `Student`: thông tin sinh viên.
- `AppUser`: tài khoản đăng nhập hệ thống.
- `Role`: vai trò phân quyền.
- `Contract`: hợp đồng nội trú.
- `ContractChangeRequest`: yêu cầu thay đổi hợp đồng.
- `UtilityRecord`: chỉ số điện nước theo phòng và kỳ.
- `Invoice`: hóa đơn theo sinh viên-phòng-tháng.
- `Issue`: yêu cầu sửa chữa/bảo trì.
- `PricingPolicy`: bảng giá điện nước, phí dịch vụ đang hiệu lực.
- `PasswordResetToken`: token đặt lại mật khẩu.

## 2) Quan hệ nghiệp vụ cốt lõi

- Một `Building` có nhiều `Room`.
- Một `RoomType` có nhiều `Room`.
- Một `Room` có nhiều `Bed`.
- Một `Student` có thể liên kết với một `AppUser` (qua `student_id`) và nhiều `Contract`.
- Một `Contract` gắn với đúng một `Student`, một `Room`, một `Bed`.
- Một `Contract` có nhiều `ContractChangeRequest`.
- Một `UtilityRecord` gắn với đúng một `Room` theo kỳ `(month, year)`.
- Một `Invoice` gắn với một `Room`, có thể gắn với một `Student`.
- Một `Issue` gắn với `Student` và `Room`.
- `AppUser` và `Role` liên kết many-to-many qua bảng `user_roles`.

## 3) Trạng thái nghiệp vụ quan trọng

- Hợp đồng (`ContractStatus`): `PENDING`, `ACTIVE`, `REJECTED`, `CANCELLED`, `EXPIRED`.
- Hóa đơn (`InvoiceStatus`): `UNPAID`, `PAID`, `OVERDUE`, `CANCELLED` (thực tế dùng trong service/repository).
- Chỉ số điện nước (`UtilityRecordStatus`): mở kỳ/chốt kỳ phục vụ sinh hóa đơn.
- Vấn đề bảo trì (`IssueStatus`): theo vòng đời tiếp nhận-xử lý-hoàn tất.

## 4) Ràng buộc dữ liệu nổi bật

Theo `schema.sql`:

- `building.name` unique.
- `room_type.name` unique.
- `student.student_code` và `student.cccd` unique.
- `users.username` unique.
- `room` unique theo cặp `(building_id, room_number)`.
- `utility_record` unique theo `(room_id, month, year)`.
- `invoice.invoice_code` unique.
- Bảng liên kết `user_roles` unique theo `(user_id, role_id)`.

## 5) Seed dữ liệu khởi tạo

Khi `app.seed.enabled=true`, hệ thống tự seed:

- Role: `ROLE_ADMIN`, `ROLE_STUDENT`.
- Tài khoản mặc định: `admin/admin123`, `student/student123`.
- Building mẫu: Tòa A, B, C, D.
- Room type mẫu: phòng 8 người, 4 người, VIP.
- Phòng và giường theo quy tắc cấu hình tầng/sức chứa.
- Sinh viên mẫu và tài khoản tương ứng theo mã sinh viên.

## 6) Lưu ý thiết kế dữ liệu

- Toàn bộ bảng chính đều có `created_at`, `updated_at`.
- Tiền tệ dùng `NUMERIC(18,2)` trong database, logic nghiệp vụ có chuẩn hóa làm tròn ở service.
- Webhook thanh toán lưu thêm dữ liệu provider (`provider_raw_payload`, transaction id) để audit.
- `utility_record.period_status` là điểm kiểm soát trước khi sinh hóa đơn.

## 7) Đề xuất chuẩn hóa thêm

- Bổ sung migration tool (Flyway/Liquibase) để version hóa schema thay cho quản lý thủ công.
- Bổ sung chỉ mục truy vấn cho các cột filter thường dùng (status, month/year, building_id).
- Chốt từ điển enum và thông điệp nghiệp vụ trong tài liệu API thống nhất.