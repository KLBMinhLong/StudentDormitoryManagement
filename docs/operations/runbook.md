# Runbook Vận Hành

## 1) Kiểm tra sức khỏe dịch vụ sau deploy

1. Kiểm tra process ứng dụng đang chạy.
2. Kiểm tra endpoint đăng nhập `/api/v1/auth/login` trả phản hồi bình thường.
3. Kiểm tra truy cập trang tĩnh `/login`, `/admin`.
4. Theo dõi log khởi động để xác nhận kết nối database thành công.

## 2) Tác vụ định kỳ

### 2.1 Hóa đơn hàng tháng

- Scheduler mặc định chạy ngày 10 hàng tháng lúc 00:30 theo `Asia/Ho_Chi_Minh`.
- Trước thời điểm chạy cần đảm bảo kỳ điện nước liên quan đã được chốt (`utility_record.period_status = CLOSED`).
- Sau khi chạy, kiểm tra số hóa đơn tạo mới qua log.

### 2.2 Vòng đời hợp đồng

- Tác vụ chạy mỗi 60 giây:
  - Hủy hợp đồng `PENDING` quá hạn giữ chỗ.
  - Chuyển hợp đồng `ACTIVE` sang hết hạn khi qua `end_date`, đồng thời giải phóng giường.

## 3) Quy trình chốt kỳ điện nước

1. Nhập/chỉnh sửa chỉ số qua nhóm API `utility-records`.
2. Kiểm tra dữ liệu trước khi khóa kỳ.
3. Gọi API `POST /api/v1/utility-records/period/close`.
4. Sinh hóa đơn theo phòng hoặc theo tháng.

## 4) Quy trình xử lý thanh toán PayOS

1. Sinh link thanh toán qua API hóa đơn của sinh viên.
2. Hệ thống nhận callback tại `/api/v1/payment/webhook/payos`.
3. Webhook luôn ACK `200` để đảm bảo tương thích provider.
4. Đối soát trạng thái hóa đơn nếu webhook lỗi chữ ký hoặc payload không hợp lệ.

## 5) Quản lý tài khoản và phân quyền

- Role chuẩn: `ROLE_ADMIN`, `ROLE_STUDENT`.
- Tài khoản seed chỉ dùng cho môi trường dev/test.
- Mọi thao tác quản trị dữ liệu cần được audit qua log và quyền truy cập.

## 6) Sao lưu và khôi phục dữ liệu

Khuyến nghị production:

- Backup PostgreSQL theo lịch hằng ngày.
- Giữ tối thiểu bản backup full và incremental theo chính sách nội bộ.
- Định kỳ diễn tập restore để đảm bảo backup dùng được.

## 7) Checklist trước release

- Đã chạy test tối thiểu: unit test hiện có và smoke test endpoint chính.
- Đã kiểm tra biến môi trường đầy đủ, đặc biệt JWT/DB/MAIL/PAYOS.
- Đã vô hiệu hoặc thay đổi thông tin seed/credential không an toàn.
- Đã cập nhật tài liệu liên quan (API, deployment, troubleshooting).