# Workflow: Vòng Đời Hợp Đồng

## 1) Mục tiêu

Chuẩn hóa vòng đời hợp đồng nội trú từ tạo mới đến hết hạn hoặc thay đổi.

## 2) Actor

- Sinh viên (`ROLE_STUDENT`)
- Quản trị (`ROLE_ADMIN`)
- Scheduler hệ thống

## 3) Trạng thái hợp đồng

- `PENDING`: đã giữ chỗ, chờ nộp/duyệt.
- `ACTIVE`: đang hiệu lực.
- `REJECTED`: bị từ chối.
- `CANCELLED`: bị hủy.
- `EXPIRED`: hết hạn theo thời gian.

## 4) Dòng chảy chính

### 4.1 Nhánh sinh viên tự giữ chỗ

1. Sinh viên gọi `POST /api/v1/contracts/reservations`.
2. Hệ thống kiểm tra:
   - Sinh viên chưa có hợp đồng mở.
   - Giường hợp lệ và còn khả dụng.
   - Giới tính sinh viên phù hợp chính sách phòng/tòa.
3. Hệ thống tạo hợp đồng `PENDING`, đặt thời hạn giữ chỗ (`hold_expires_at`).
4. Hệ thống khóa giường tạm thời cho hợp đồng đó.

### 4.2 Nhánh nộp hồ sơ và duyệt

1. Sinh viên nộp thông tin qua `PUT /api/v1/contracts/{contractId}/submit`.
2. Admin xem danh sách chờ duyệt và xử lý:
   - Duyệt: `PUT /api/v1/contracts/{contractId}/approve`.
   - Từ chối: `PUT /api/v1/contracts/{contractId}/reject`.
3. Khi duyệt:
   - Trạng thái thành `ACTIVE`.
   - Gắn giường chính thức cho sinh viên.
   - Cập nhật trạng thái phòng theo số giường còn trống.

### 4.3 Nhánh thay đổi hợp đồng

1. Sinh viên gửi yêu cầu thay đổi qua `POST /api/v1/contracts/{contractId}/change-requests`.
2. Admin xử lý tại:
   - `PUT /api/v1/contracts/change-requests/{requestId}/approve`
   - `PUT /api/v1/contracts/change-requests/{requestId}/reject`
3. Hệ thống cập nhật dữ liệu hợp đồng và lịch sử yêu cầu thay đổi.

### 4.4 Nhánh hết hạn tự động

1. Scheduler chạy mỗi 60 giây.
2. Hủy hợp đồng `PENDING` quá hạn giữ chỗ.
3. Chuyển hợp đồng `ACTIVE` quá hạn sang `EXPIRED`.
4. Giải phóng giường tương ứng và cập nhật trạng thái phòng.

## 5) API liên quan

- Sinh viên: `/api/v1/contracts/me/*`, `/api/v1/contracts/{contractId}/submit`, `/api/v1/contracts/{contractId}/change-requests`
- Quản trị: `/api/v1/contracts/pending`, `/api/v1/contracts/admin/management`, `/api/v1/contracts/*/approve`, `/api/v1/contracts/*/reject`

## 6) Rủi ro nghiệp vụ cần kiểm soát

- Race condition khi nhiều người đặt cùng một giường.
- Không đồng bộ giữa trạng thái hợp đồng và trạng thái giường.
- Duyệt hợp đồng khi yêu cầu giữ chỗ đã quá hạn.
- Thay đổi hợp đồng nhưng không cập nhật dữ liệu liên quan (room/bed/history).