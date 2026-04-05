# Quy Trình Phát Triển

## 1) Vòng đời công việc

1. Làm rõ yêu cầu nghiệp vụ và tiêu chí chấp nhận.
2. Thiết kế thay đổi (API, DB, UI, security) trước khi code.
3. Triển khai theo chuẩn DTO-first và chuẩn response thống nhất.
4. Viết hoặc cập nhật test liên quan.
5. Tự kiểm tra local (build, test, smoke test).
6. Tạo pull request, review chéo.
7. Hợp nhất và phát hành.

## 2) Quy ước nhánh

Gợi ý:

- `main`: nhánh ổn định.
- `feature/<ten-tinh-nang>`: phát triển tính năng.
- `bugfix/<ten-loi>`: sửa lỗi.
- `hotfix/<ten-khan-cap>`: xử lý sự cố production.

## 3) Quy ước commit

Nên dùng tiền tố rõ ràng:

- `feat:` tính năng mới.
- `fix:` sửa lỗi.
- `refactor:` tái cấu trúc không đổi hành vi.
- `docs:` cập nhật tài liệu.
- `test:` bổ sung/chỉnh sửa kiểm thử.
- `chore:` việc kỹ thuật khác.

Ví dụ:

- `feat: bo sung API timeline dien nuoc cho sinh vien`
- `fix: xu ly dung timezone khi sinh hoa don`

## 4) Checklist pull request

- Đúng naming convention của dự án.
- Controller trả `ApiResponse<T>` với mã phù hợp.
- Không trả trực tiếp entity từ controller.
- Endpoint list có phân trang theo chuẩn nếu trả danh sách.
- Có kiểm tra quyền truy cập phù hợp.
- Không chứa credential/secret thật.
- Đã cập nhật tài liệu ở `docs` nếu có thay đổi hành vi.

## 5) Quy trình review

- Review tập trung vào tính đúng nghiệp vụ, bảo mật, khả năng bảo trì.
- Ưu tiên phát hiện regression và null/edge-case.
- Nếu có thay đổi schema hoặc contract API, bắt buộc reviewer xác nhận tương thích.

## 6) Quy trình release

1. Chốt phạm vi release.
2. Chạy build và test.
3. Kiểm tra biến môi trường production.
4. Deploy theo run-guide.
5. Smoke test endpoint quan trọng.
6. Theo dõi log 15-30 phút đầu.

## 7) Chính sách tài liệu

Mọi thay đổi trong các nhóm sau phải cập nhật docs cùng lúc:

- Endpoint hoặc payload API.
- Security/policy phân quyền.
- Cấu hình profile, biến môi trường.
- Scheduler hoặc luồng thanh toán.
- Giao diện và quy chuẩn UI.