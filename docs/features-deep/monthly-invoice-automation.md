# Tính Năng: Tự Động Tính Hóa Đơn Từng Tháng

## 1) Mục tiêu

Tự động sinh hóa đơn định kỳ cho tháng trước, dựa trên chỉ số điện nước đã chốt và danh sách hợp đồng còn hiệu lực.

## 2) Luồng xử lý tổng quát

1. Scheduler chạy theo cron cấu hình (mặc định: 00:30 ngày 10 hàng tháng).
2. Hệ thống xác định kỳ cần tính = tháng trước thời điểm chạy.
3. Gọi service sinh hóa đơn theo kỳ.
4. Service duyệt từng utility record theo kỳ:
   - Chỉ xử lý khi kỳ ở trạng thái `CLOSED`.
   - Lấy danh sách hợp đồng `ACTIVE` của phòng.
   - Tính điện/nước tiêu thụ và chia theo số sinh viên active.
   - Cộng phí dịch vụ theo pricing policy.
   - Bỏ qua nếu hóa đơn đã tồn tại (trừ `CANCELLED`).
5. Lưu hóa đơn mới với trạng thái `UNPAID` và hạn thanh toán.
6. Ghi log số hóa đơn đã tạo.

## 3) Điều kiện nghiệp vụ quan trọng

- Kỳ điện nước phải được chốt (`UtilityRecordStatus.CLOSED`).
- Phòng phải có ít nhất 1 hợp đồng `ACTIVE`.
- Không tạo trùng hóa đơn theo sinh viên-phòng-tháng-năm.
- Giá điện/nước/phí dịch vụ lấy từ pricing policy hiện hành.

## 4) Vị trí code rõ ràng

### 4.1 Điểm kích hoạt tự động

- File: `src/main/java/com/dormitory/management/service/InvoiceGenerationScheduler.java`
- Class: `InvoiceGenerationScheduler`
- Method: `generateMonthlyInvoicesOnDay10`
- Line chính:
  - Khai báo scheduler cron: dòng 20-22
  - Tạo request tháng trước: dòng 25-31
  - Gọi service sinh hóa đơn: dòng 35

### 4.2 Nghiệp vụ sinh hóa đơn cốt lõi

- File: `src/main/java/com/dormitory/management/service/impl/InvoiceServiceImpl.java`
- Class: `InvoiceServiceImpl`
- Method: `generateMonthlyInvoices`
- Line chính:
  - Bắt đầu method: dòng 228
  - Lọc kỳ chưa chốt: dòng 237-239
  - Lấy hợp đồng active theo phòng: dòng 241-248
  - Tính điện/nước và utility tổng: dòng 250-257
  - Chia chi phí theo số sinh viên: dòng 258-260
  - Chống tạo trùng hóa đơn: dòng 268-276
  - Tính tổng tiền + due date: dòng 281-287
  - Tạo và lưu invoice: dòng 289-312

### 4.3 Điểm kích hoạt thủ công (admin)

- File: `src/main/java/com/dormitory/management/controller/InvoiceController.java`
- Class: `InvoiceController`
- Method: `generateMonthlyInvoices`
- Line chính:
  - Endpoint: `POST /api/v1/invoices/generate-monthly`
  - Dòng 45-49

## 5) Cấu hình liên quan

- File: `src/main/resources/application.properties`
  - `app.scheduler.invoice-generation.cron`
  - `app.scheduler.zone`

## 6) Tình huống lỗi/thực tế cần lưu ý

- Utility record chưa chốt kỳ: bị bỏ qua, không tạo invoice.
- Không có hợp đồng active: phòng bị bỏ qua.
- Dữ liệu student null trong contract: contract đó bị bỏ qua.
- Lỗi runtime khi scheduler chạy: được catch và log, không dừng ứng dụng.

## 7) Checklist kiểm thử

- Kỳ đã chốt và có hợp đồng active => sinh đúng số hóa đơn.
- Kỳ chưa chốt => không sinh.
- Chạy lặp lại cùng kỳ => không tạo trùng.
- Kiểm tra tổng tiền = utility chia đầu người + service fee.
- Kiểm tra due date = ngày phát hành + 10 ngày (23:59:59).