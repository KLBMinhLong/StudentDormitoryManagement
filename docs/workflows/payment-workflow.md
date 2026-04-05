# Workflow: Thanh Toán PayOS

## 1) Mục tiêu

Mô tả luồng thanh toán hóa đơn qua PayOS, từ tạo payment link đến cập nhật trạng thái hóa đơn sau webhook.

## 2) Actor

- Sinh viên
- Hệ thống nội bộ
- PayOS

## 3) Điều kiện đầu vào

- Hóa đơn thuộc sinh viên đang đăng nhập.
- Hóa đơn ở trạng thái cho phép thanh toán (`UNPAID` hoặc `OVERDUE`).
- Cấu hình PayOS hợp lệ (`client-id`, `api-key`, `checksum-key`, `return-url`, `cancel-url`).

## 4) Dòng chảy chính

1. Sinh viên gọi `POST /api/v1/invoices/me/{invoiceId}/create-payment-link`.
2. Hệ thống tạo order code và gọi API PayOS để lấy link thanh toán.
3. Hệ thống lưu thông tin link và metadata provider vào bản ghi hóa đơn.
4. Sinh viên thanh toán trên cổng PayOS.
5. PayOS callback webhook về `POST /api/v1/payment/webhook/payos`.
6. Hệ thống xác minh chữ ký, đọc payload, đối chiếu invoice/order.
7. Nếu hợp lệ và thanh toán thành công:
   - Cập nhật `invoice.status = PAID`.
   - Cập nhật `paid_at`, `provider_transaction_id`, payload đối soát.
8. Hệ thống trả ACK 200 cho webhook.

## 5) Nhánh ngoại lệ

- Webhook payload lỗi hoặc thiếu chữ ký:
  - Ghi log cảnh báo.
  - Vẫn ACK 200 để tương thích kênh webhook.
  - Không cập nhật sai trạng thái hóa đơn.
- Invoice không khớp order code: bỏ qua cập nhật, log để điều tra.
- Provider timeout: có thể tạo lại payment link theo chính sách nghiệp vụ.

## 6) Điểm kiểm soát vận hành

- URL webhook phải public và ổn định.
- Giám sát log webhook để phát hiện lỗi chữ ký hoặc mapping.
- Không log lộ secret checksum key.
- Định kỳ đối soát giao dịch provider với dữ liệu nội bộ.

## 7) API liên quan

- `POST /api/v1/invoices/me/{invoiceId}/create-payment-link`
- `POST /api/v1/payment/webhook/payos`
- `POST /api/v1/invoices/{invoiceId}/manual-approve` (fallback xác nhận thủ công)