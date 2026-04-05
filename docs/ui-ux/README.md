# UI/UX Guidelines

Tài liệu này chuẩn hóa giao diện trong dự án ký túc xá, đồng bộ với các quy định hiện có trong `CODING_STANDARDS.md`.

## 1) Thiết kế nhất quán

- Dùng token màu cố định của dự án.
- Typography:
  - Heading: Be Vietnam Pro.
  - Body/UI: Inter.
- Radius chuẩn: 12px.
- Shadow chuẩn: `0 4px 12px rgba(15, 23, 42, 0.06)`.
- Nền trang sáng, nhẹ, ưu tiên tính dễ đọc dữ liệu.

## 2) Cấu trúc giao diện hiện tại

- Trang dùng chung/public: `static/login.html`, `static/register.html`, ...
- Khu vực admin: `static/admin/*`.
- Khu vực sinh viên: `static/user/*`.
- Thành phần UI dùng lại: `static/ui/theme.css`, `static/ui/admin-sidebar.js`, `static/ui/user-sidebar.js`.

## 3) Chuẩn nội dung tiếng Việt

- Toàn bộ nhãn, tiêu đề, thông báo phải có dấu đầy đủ.
- Không dùng tiếng Việt không dấu trên UI.
- Dữ liệu người dùng nhập có dấu phải được giữ nguyên.

## 4) Chuẩn component

- Button chính: màu primary, trạng thái hover rõ ràng.
- Input: viền rõ, focus-visible.
- Card: nền trắng, bo góc 12px, bóng mềm.
- Table: dễ đọc, hỗ trợ phân trang chuẩn.
- Badge trạng thái: có text rõ nghĩa, không dùng màu đơn lẻ để truyền tải trạng thái.

## 5) Chuẩn phân trang UI

- Luôn có Previous/Next và chỉ số trang.
- Hiển thị thông tin: `Hiển thị X-Y trên tổng số Z kết quả`.
- Khi đổi trang, cuộn mượt lên đầu danh sách.

## 6) Khả năng truy cập

- Điều hướng được bằng bàn phím.
- Focus ring phải nhìn thấy rõ.
- Tương phản màu đủ cao trên nền sáng.
- Dùng semantic HTML hợp lý theo ngữ cảnh.

## 7) Tài liệu liên quan

- [UI_QUICK_START.md](UI_QUICK_START.md)
- [TAILWIND_THEME_SNIPPET.md](TAILWIND_THEME_SNIPPET.md)