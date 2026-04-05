# Workflow: Hành Trình Sinh Viên

## 1) Mục tiêu

Mô tả luồng nghiệp vụ của một sinh viên từ lúc tạo tài khoản đến lúc theo dõi hợp đồng, hóa đơn, hồ sơ cư trú.

## 2) Actor

- Sinh viên (`ROLE_STUDENT`)
- Quản trị (`ROLE_ADMIN`) ở các bước duyệt/hỗ trợ

## 3) Điều kiện đầu vào

- Sinh viên có tài khoản đăng nhập.
- Hồ sơ sinh viên đã tồn tại hoặc được tạo bởi admin.
- Phòng/giường còn khả dụng.

## 4) Dòng chảy chính

1. Sinh viên đăng nhập qua `POST /api/v1/auth/login`.
2. Sinh viên xem danh sách phòng và giường khả dụng.
3. Sinh viên gửi yêu cầu giữ chỗ qua `POST /api/v1/contracts/reservations`.
4. Sinh viên nộp hồ sơ hợp đồng qua `PUT /api/v1/contracts/{contractId}/submit`.
5. Admin duyệt hợp đồng, hợp đồng chuyển `ACTIVE`, giường gắn với sinh viên.
6. Sinh viên theo dõi hợp đồng của mình qua `GET /api/v1/contracts/me/contracts`.
7. Hằng tháng sinh viên xem hóa đơn qua `GET /api/v1/invoices/me`.
8. Sinh viên tạo link thanh toán qua `POST /api/v1/invoices/me/{invoiceId}/create-payment-link`.
9. Sinh viên cập nhật hồ sơ cá nhân khi cần qua `PUT /api/v1/students/me`.

## 5) Nhánh ngoại lệ

- Giường đã có người giữ/chưa giải phóng: từ chối đặt chỗ.
- Quá thời gian giữ chỗ: hợp đồng `PENDING` bị hủy tự động.
- Hồ sơ nộp thiếu thông tin bắt buộc: trả lỗi 400.
- Token hết hạn/thiếu quyền: trả 401/403.

## 6) Dữ liệu và trạng thái bị tác động

- `contract.status`: `PENDING -> ACTIVE` hoặc `PENDING -> REJECTED/CANCELLED`.
- `bed`: cập nhật `is_occupied`, `student_id`, `reserved_until`.
- `invoice.status`: `UNPAID/OVERDUE -> PAID` khi thanh toán thành công.

## 7) Điểm kiểm soát QA

- Không cho sinh viên có nhiều hợp đồng mở cùng lúc.
- Không cho đặt giường khác giới tính chính sách phòng/tòa.
- Không cho sinh viên truy cập dữ liệu của sinh viên khác.
- Luồng thanh toán không làm thay đổi nhầm hóa đơn khác kỳ.