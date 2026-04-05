# Tổng Quan Dòng Chảy Nghiệp Vụ

Tài liệu này tổng hợp các luồng nghiệp vụ quan trọng nhất của hệ thống, làm điểm vào cho BA, Dev, QA, vận hành.

## 1) Danh sách luồng trọng yếu

- Luồng hành trình sinh viên: từ đăng ký/đăng nhập đến ở nội trú và thanh toán.
- Luồng vòng đời hợp đồng: giữ chỗ, nộp hồ sơ, duyệt/từ chối, thay đổi, hết hạn.
- Luồng điện nước và hóa đơn: nhập chỉ số, chốt kỳ, sinh hóa đơn, quá hạn.
- Luồng thanh toán PayOS: tạo payment link, webhook cập nhật trạng thái.

## 2) Mối liên hệ giữa các luồng

1. Sinh viên cần có hợp đồng `ACTIVE` để phát sinh hóa đơn định kỳ.
2. Hóa đơn chỉ được sinh khi kỳ điện nước đã `CLOSED`.
3. Thanh toán thành công cập nhật trạng thái hóa đơn từ `UNPAID`/`OVERDUE` sang `PAID`.
4. Hợp đồng hết hạn sẽ giải phóng giường, ảnh hưởng dữ liệu cư trú và hóa đơn các kỳ tiếp theo.

## 3) Tài liệu chi tiết

- [student-journey-workflow.md](student-journey-workflow.md)
- [contract-lifecycle-workflow.md](contract-lifecycle-workflow.md)
- [utility-invoice-workflow.md](utility-invoice-workflow.md)
- [payment-workflow.md](payment-workflow.md)

## 4) Quy tắc đọc tài liệu workflow

Mỗi luồng đều có cấu trúc chuẩn:

- Mục tiêu và actor.
- Điều kiện đầu vào.
- Dòng chảy chính.
- Nhánh ngoại lệ.
- Trạng thái dữ liệu bị tác động.
- API liên quan và điểm kiểm soát vận hành.