# Nghiệp vụ điện nước hàng tháng (chuẩn vận hành)

## 1. Mục tiêu
- Chuẩn hóa quy trình nhập chỉ số điện nước theo tháng.
- Giảm sai số nhập liệu, tăng tốc thao tác cho Admin.
- Tạo nền tảng cho bước sinh hóa đơn cá nhân tự động ở giai đoạn tiếp theo.

## 2. Quy trình 4 bước

### Bước 1: Lấy chỉ số cũ (Auto-fill)
- Khi chọn phòng + tháng/năm, hệ thống tìm bản ghi gần nhất trước kỳ hiện tại.
- Gán `oldElectric = newElectric_truoc_do`, `oldWater = newWater_truoc_do`.
- Nếu chưa có lịch sử: cho phép nhập tay chỉ số cũ.

### Bước 2: Nhập và kiểm tra (Validation)
- Điều kiện bắt buộc:
  - `newElectric >= oldElectric`
  - `newWater >= oldWater`
- Cảnh báo tăng đột biến (không chặn cứng):
  - Nếu tiêu thụ điện kỳ này > 5 lần tiêu thụ điện kỳ trước => cảnh báo xác nhận.
  - Nếu tiêu thụ nước kỳ này > 5 lần tiêu thụ nước kỳ trước => cảnh báo xác nhận.

### Bước 3: Tính toán và chia tiền (chưa code, chỉ chốt nghiệp vụ)
- `totalElectric = (newElectric - oldElectric) * unitPriceElectric`
- `totalWater = (newWater - oldWater) * unitPriceWater`
- `totalUtility = totalElectric + totalWater`
- Lấy danh sách sinh viên có hợp đồng `ACTIVE` tại phòng ở thời điểm chốt kỳ.
- `amountPerStudent = totalUtility / numberOfStudents`.

### Bước 4: Sinh hóa đơn cá nhân (chưa code, chỉ chốt nghiệp vụ)
- Tạo hóa đơn cho từng sinh viên với trạng thái `UNPAID`.
- Mỗi hóa đơn lưu snapshot dữ liệu chia tại thời điểm sinh:
  - roomId, month, year
  - electricUsage, waterUsage
  - unitPriceElectric, unitPriceWater
  - amountBeforeAdjustments, amountFinal
  - status = UNPAID

## 3. UI/UX nhập liệu Admin

### 3.1 Nhập đơn lẻ
- Chọn phòng, tháng, năm.
- Hệ thống tự điền chỉ số cũ.
- Admin nhập chỉ số mới và xác nhận.
- Nếu tăng đột biến, hiển thị hộp xác nhận trước khi lưu.

### 3.2 Nhập hàng loạt theo tòa (Bulk Entry)
- Chọn tòa + tháng + năm.
- Hiển thị toàn bộ phòng của tòa trong bảng.
- Mỗi dòng có:
  - Chỉ số cũ điện/nước (prefill)
  - Chỉ số mới điện/nước (Admin nhập nhanh)
- Admin thao tác tab từ trên xuống và lưu một lần.

## 4. Tình huống thực tế bắt buộc hỗ trợ

### 4.1 Phòng trống
- Nếu phòng không có hợp đồng ACTIVE ở kỳ đó:
  - Vẫn cho phép lưu chỉ số vận hành.
  - Khi sang bước hóa đơn: không sinh hóa đơn sinh viên.
  - Ghi nhận chi phí vào nhóm "chi phí vận hành/chung" (cấu hình sau).

### 4.2 Nhập sai sau khi đã chốt kỳ
- Cần có luồng "Hủy chốt kỳ & nhập lại":
  - Xóa hóa đơn cá nhân phát sinh từ kỳ sai.
  - Đặt bản ghi utility kỳ đó về trạng thái có thể chỉnh sửa.
  - Ghi log ai hủy, thời gian, lý do.

## 5. Đề xuất siết chặt thêm
- Khóa kỳ đã chốt để tránh sửa trực tiếp.
- Audit log cho thao tác nhập/chỉnh/xóa/chốt.
- Idempotent key cho thao tác chốt kỳ để chống bấm lặp.
- Màn hình đối soát trước khi sinh hóa đơn:
  - Tổng phòng có dữ liệu
  - Phòng thiếu dữ liệu
  - Phòng tăng đột biến

## 6. API giai đoạn hiện tại
- `GET /api/v1/utility-records/prefill`:
  - Input: roomId, month, year
  - Output: chỉ số cũ gợi ý, kỳ trước, mức tiêu thụ kỳ trước
- `POST /api/v1/utility-records/batch`:
  - Nhập hàng loạt theo tòa
- `POST/PUT /api/v1/utility-records`:
  - Nhập đơn lẻ, cập nhật đơn lẻ

## 7. API giai đoạn sau (hóa đơn)
- Chưa code ở đợt này, chỉ chốt nghiệp vụ:
  - `POST /api/v1/utility-billing/close-period`
  - `POST /api/v1/utility-billing/reopen-period`
  - `GET /api/v1/utility-billing/preview`

## 8. Chính sách giá và công thức tính hóa đơn

### 8.1 Đơn giá áp dụng
- Tiền điện: `3.500 VNĐ / kWh`.
- Tiền nước: `20.000 VNĐ / m3`.
- Phí dịch vụ cố định: `50.000 VNĐ / sinh viên / tháng`.
- Nếu từng cơ sở đang áp dụng `15.000 VNĐ / m3`, giữ nguyên công thức và cấu hình lại `waterUnitPriceSnapshot` theo cơ sở đó.

### 8.2 Công thức chuẩn
- Định nghĩa:
  - `ElectricUsage = newElectric - oldElectric`
  - `WaterUsage = newWater - oldWater`
  - `StudentsInRoom`: số sinh viên có hợp đồng `ACTIVE` tại thời điểm chốt kỳ.
  - `RoomPrice`: tiền phòng mỗi sinh viên theo loại phòng/hợp đồng.
- Công thức tính tiền phải thu mỗi sinh viên:

$$
Total = RoomPrice + \frac{(ElectricUsage \times 3500) + (WaterUsage \times 20000)}{StudentsInRoom} + ServiceFee
$$

- Trong đó `ServiceFee = 50.000`.
- Làm tròn tiền:
  - Làm tròn đến đơn vị VNĐ (0 số lẻ).
  - Quy tắc chuẩn: `round half up`.

### 8.3 Trường hợp phòng trống
- Nếu `StudentsInRoom = 0`:
  - Không tạo hóa đơn sinh viên.
  - Điện/nước kỳ đó được ghi nhận vào chi phí vận hành tòa nhà.

## 9. Khi nào hóa đơn tháng được tạo

### 9.1 Điều kiện tạo hóa đơn
- Hóa đơn tháng `M` chỉ được tạo khi:
  - Chỉ số điện nước tháng `M` đã nhập đầy đủ cho các phòng cần thu.
  - Kỳ điện nước tháng `M` đã được `chốt kỳ` (status utility = `CLOSED`).
  - Có ít nhất 1 hợp đồng `ACTIVE` trong phòng tại thời điểm chốt kỳ.

### 9.2 Thời điểm tạo hóa đơn
- Job tự động chạy lúc `00:30` ngày đầu tháng kế tiếp.
- Dữ liệu tạo hóa đơn cho tháng `M` được sinh trong tháng `M+1`.
- Ví dụ:
  - Chỉ số tháng 03/2026 -> chốt kỳ trong cuối tháng 03.
  - Hệ thống tạo hóa đơn vào `00:30` ngày 01/04/2026.

### 9.3 Quy tắc idempotent
- Mỗi bộ `studentId + roomId + month + year` chỉ có 1 hóa đơn hiệu lực.
- Chạy lại job không tạo trùng.

## 10. Hạn thanh toán và trạng thái quá hạn

### 10.1 Mốc thời gian
- `issuedAt`: thời điểm hóa đơn được tạo.
- `dueAt`: mặc định `23:59:59` ngày 10 của tháng kế tiếp.
- `overdueAt`: bằng `dueAt`.

### 10.2 Trạng thái hóa đơn
- `DRAFT`: bản nháp nội bộ trước khi phát hành.
- `UNPAID`: đã phát hành, chưa thanh toán, còn trong hạn.
- `PAID`: đã thanh toán thành công.
- `OVERDUE`: quá hạn mà chưa thanh toán.
- `CANCELLED`: hủy do mở lại kỳ/chỉnh dữ liệu.

### 10.3 Chuyển trạng thái tự động
- `UNPAID -> OVERDUE` khi `now > dueAt` và chưa có giao dịch thành công.
- `OVERDUE -> PAID` nếu thanh toán thành công sau hạn.
- Khi thanh toán sau hạn, lưu thêm `paidLate = true` để phục vụ báo cáo.

## 11. Luồng thanh toán tự động qua cổng trung gian (PayOS)

### 11.1 Sequence vận hành
1. Hệ thống tạo `Invoice` định kỳ hàng tháng.
2. Sinh viên nhấn `Thanh toán` trên Web.
3. Spring Boot gọi API PayOS để tạo `paymentLink` hoặc `QR động` duy nhất cho hóa đơn.
4. Sinh viên quét QR, xác nhận chuyển tiền trên ứng dụng ngân hàng.
5. Ngân hàng xác nhận giao dịch với PayOS.
6. PayOS gọi webhook về Spring Boot với sự kiện `payment_success`.
7. Spring Boot xác thực webhook, cập nhật hóa đơn sang `PAID`, ghi nhận giao dịch và gửi thông báo.

### 11.2 Quy tắc kỹ thuật quan trọng
- Mỗi lần tạo link thanh toán phải có `orderCode` duy nhất.
- Webhook phải kiểm tra chữ ký (`signature`) từ PayOS.
- Webhook xử lý idempotent theo `transactionId` hoặc `orderCode`.
- Nếu hóa đơn đã `PAID`, webhook lặp lại chỉ ghi log, không xử lý lại.

## 12. Cấu trúc Invoice nâng cấp (đề xuất)
- Nhóm định danh:
  - `id`, `invoiceCode`, `studentId`, `roomId`, `month`, `year`.
- Nhóm snapshot tính tiền:
  - `roomPriceSnapshot`
  - `electricUsageSnapshot`, `waterUsageSnapshot`
  - `electricUnitPriceSnapshot` (=3500)
  - `waterUnitPriceSnapshot` (=20000)
  - `serviceFeeSnapshot` (=50000)
  - `studentsInRoomSnapshot`
  - `utilityAmountPerStudent`, `totalAmount`
- Nhóm trạng thái thời gian:
  - `status` (`UNPAID|PAID|OVERDUE|CANCELLED`)
  - `issuedAt`, `dueAt`, `paidAt`, `overdueMarkedAt`
  - `paidLate` (boolean)
- Nhóm tích hợp thanh toán:
  - `paymentProvider` (PAYOS)
  - `paymentOrderCode`
  - `paymentLink`
  - `paymentQrCode`
  - `providerTransactionId`
  - `providerRawPayload` (JSON, optional)
- Nhóm duyệt thủ công:
  - `manualApprovedBy`
  - `manualApprovedAt`
  - `manualApprovalNote`

## 13. Logic Webhook (trọng tâm tự động hóa)

### 13.1 Endpoint đề xuất
- `POST /api/v1/payment/webhook/payos`

### 13.2 Luồng xử lý chuẩn
1. Nhận JSON webhook từ PayOS.
2. Xác thực chữ ký và timestamp chống replay.
3. Kiểm tra event type:
  - `payment_success`: xử lý thành công.
  - Event khác: ghi log, trả `200` để tránh retry vô hạn.
4. Tìm hóa đơn theo `paymentOrderCode`.
5. Kiểm tra số tiền khớp `totalAmount` (sai lệch -> trạng thái `PAYMENT_MISMATCH` trong log nghiệp vụ).
6. Nếu hợp lệ:
  - Cập nhật `status = PAID`
  - Set `paidAt = now`
  - Set `providerTransactionId`
  - Lưu `providerRawPayload`
7. Gửi thông báo cho sinh viên (in-app + email nếu có).
8. Trả HTTP `200 OK`.

### 13.3 Yêu cầu an toàn
- Không cập nhật trạng thái nếu chưa xác thực chữ ký.
- Không tin dữ liệu amount từ client frontend, chỉ tin webhook đã ký.
- Lưu bảng audit cho mọi webhook vào/ra.

## 14. Xử lý bất khả kháng: duyệt thủ công bởi Admin

### 14.1 Màn hình Admin Dashboard
- Bảng hóa đơn có bộ lọc theo trạng thái: `UNPAID`, `OVERDUE`.
- Mỗi dòng có nút `Xác nhận thủ công`.

### 14.2 Quy tắc khi xác nhận thủ công
- Bắt buộc nhập `Ghi chú lý do duyệt tay`.
- Có thể đính kèm minh chứng (ảnh biên lai/chuyển khoản) ở giai đoạn sau.
- Chỉ role `ROLE_ADMIN` hoặc `ROLE_ACCOUNTANT` mới được thao tác.

### 14.3 Kết quả hệ thống
- Cập nhật:
  - `status = PAID`
  - `paidAt = now`
  - `paymentProvider = MANUAL`
  - `manualApprovedBy`, `manualApprovedAt`, `manualApprovalNote`
- Ghi audit log đầy đủ: ai duyệt, thời gian, lý do, hóa đơn nào.

## 15. API giai đoạn hóa đơn đề xuất chi tiết
- `POST /api/v1/utility-billing/generate-monthly`:
  - Sinh hóa đơn tháng theo kỳ đã chốt.
- `GET /api/v1/invoices/me?month=&year=&status=`:
  - Sinh viên xem danh sách hóa đơn.
- `POST /api/v1/invoices/{id}/create-payment-link`:
  - Tạo link/QR thanh toán PayOS cho hóa đơn.
- `POST /api/v1/payment/webhook/payos`:
  - Endpoint nhận callback từ PayOS.
- `POST /api/v1/invoices/{id}/manual-approve`:
  - Admin xác nhận thanh toán thủ công (bắt buộc note).
- `POST /api/v1/invoices/mark-overdue`:
  - Job đánh dấu quá hạn theo `dueAt`.

## 16. KPI vận hành nên theo dõi
- Tỷ lệ hóa đơn thanh toán đúng hạn.
- Tỷ lệ hóa đơn `OVERDUE` theo tòa nhà/tháng.
- Tỷ lệ thanh toán tự động vs duyệt thủ công.
- Số webhook lỗi chữ ký/sai số tiền.
