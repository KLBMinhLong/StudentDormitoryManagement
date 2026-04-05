# Workflow: Điện Nước Và Hóa Đơn

## 1) Mục tiêu

Chuẩn hóa luồng dữ liệu từ nhập chỉ số điện nước đến phát hành và theo dõi hóa đơn.

## 2) Actor

- Quản trị (`ROLE_ADMIN`)
- Scheduler hệ thống
- Sinh viên (xem/chi trả hóa đơn)

## 3) Điều kiện đầu vào

- Có bản ghi `utility_record` hợp lệ theo kỳ tháng/năm.
- Kỳ cần sinh hóa đơn đã ở trạng thái `CLOSED`.
- Phòng có sinh viên với hợp đồng `ACTIVE`.
- Có pricing policy hiệu lực.

## 4) Dòng chảy chính

1. Admin nhập chỉ số từng phòng hoặc hàng loạt:
   - `POST /api/v1/utility-records`
   - `POST /api/v1/utility-records/batch`
2. Admin kiểm tra lịch sử/chênh lệch qua:
   - `GET /api/v1/utility-records/history`
   - `GET /api/v1/utility-records/timeline`
3. Admin chốt kỳ điện nước:
   - `POST /api/v1/utility-records/period/close`
4. Sinh hóa đơn:
   - Tự động theo lịch: scheduler ngày 10 hằng tháng.
   - Hoặc thủ công: `POST /api/v1/invoices/generate-monthly` hoặc theo phòng.
5. Hệ thống tính toán:
   - Điện/nước tiêu thụ theo chênh lệch chỉ số.
   - Chia chi phí theo số sinh viên có hợp đồng active trong phòng.
   - Cộng phí dịch vụ theo pricing policy.
6. Admin theo dõi hóa đơn qua `GET /api/v1/invoices/admin/list`.
7. Hệ thống đánh dấu quá hạn qua `POST /api/v1/invoices/admin/mark-overdue` hoặc tác vụ tương ứng.

## 5) Nhánh ngoại lệ

- Utility record chưa chốt kỳ: không cho sinh hóa đơn.
- Không có hợp đồng active trong phòng: bỏ qua phòng đó.
- Hóa đơn kỳ đã tồn tại (trừ đã hủy): không tạo trùng.
- Chỉ số mới nhỏ hơn chỉ số cũ: cần chặn và trả lỗi nghiệp vụ.

## 6) Điểm kiểm soát dữ liệu

- Unique utility record theo `(room_id, month, year)`.
- Unique invoice code.
- Không tạo nhiều hóa đơn cùng kỳ cho cùng sinh viên-phòng.
- Lưu đầy đủ trường theo dõi thanh toán, quá hạn, đối soát provider.

## 7) Kiểm thử khuyến nghị

- Test case chốt kỳ và sinh hóa đơn liên phòng.
- Test case sinh hóa đơn khi thay đổi pricing policy.
- Test case phòng có 0, 1, nhiều sinh viên active.
- Test case overdue và manual approve.