# Coding Standards - Student Dormitory Management

## 1) Naming Convention

- Java class names: PascalCase.
- Java method and variable names: camelCase.
- Database table and column names: snake_case (khai báo rõ bằng `@Table`, `@Column`).
- API endpoint: kebab-case, base prefix `/api/v1`.

## 2) API Response Standard

- Bắt buộc dùng `ApiResponse<T>` với đúng 3 field: `code`, `message`, `result`.
- Mọi controller API JSON trả `ResponseEntity<ApiResponse<T>>`.
- Quy ước code thành công:
  - GET/PUT: `200`
  - POST: `201`
  - DELETE: `204` với `result = null`

## 3) Exception Handling Standard

- Bắt buộc xử lý lỗi tập trung bằng `@RestControllerAdvice`.
- Chuẩn mã lỗi tối thiểu:
  - `400`: validation hoặc lỗi nghiệp vụ đầu vào
  - `404`: không tìm thấy tài nguyên
  - `500`: lỗi hệ thống
- Response lỗi vẫn dùng `ApiResponse<T>`.

## 4) DTO-First Rule

- Không expose JPA Entity trực tiếp từ controller.
- Input dùng `*RequestDTO`.
- Output dùng `*ResponseDTO` hoặc DTO nghiệp vụ tương đương.
- Mapping xử lý ở service layer.

## 5) Pagination Rule

- Tất cả API trả danh sách phải có phân trang.
- Tham số mặc định:
  - `page = 0`
  - `size = 10`
  - `sortBy = id` hoặc `createdAt`
  - `direction = desc`
- Kết quả phân trang bọc bằng `PagedResponseDTO<T>` và đặt trong `ApiResponse.result`.

## 6) Security Rule

- Cơ chế xác thực chuẩn: JWT Bearer token.
- Endpoint cần bảo vệ phải khai báo rõ quyền (`ROLE_ADMIN`, `ROLE_STUDENT`).
- Không hardcode secret trong code; dùng biến môi trường.

## 7) Service and Repository Rule

- Service chứa nghiệp vụ, transaction boundary và chuẩn hóa dữ liệu.
- Repository chỉ tập trung truy vấn và không chứa logic nghiệp vụ phức tạp.
- Luôn kiểm tra trạng thái nghiệp vụ trước khi chuyển bước vòng đời (hợp đồng, hóa đơn, chỉ số điện nước).

## 8) Database and Schema Rule

- Database hiện tại: PostgreSQL.
- Không dùng `ddl-auto=create` trong production.
- Mọi thay đổi schema cần được tài liệu hóa ở `docs/architecture` và `docs/deployment`.

## 9) Testing Rule

- Tối thiểu phải có unit test cho service cốt lõi khi thêm logic mới.
- Với thay đổi ảnh hưởng endpoint quan trọng, cần smoke test API trước merge.
- Không merge nếu không build được bằng Maven wrapper.

## 10) Logging and Observability Rule

- Log cần đủ thông tin để truy vết nhưng không lộ dữ liệu nhạy cảm.
- Không log secret, token, password, checksum key.
- Các tác vụ scheduler cần log số lượng bản ghi đã xử lý.

## 11) UI Consistency Rule

- Stack mặc định: `html-tailwind` (trừ khi có yêu cầu khác).
- Thiết kế nhẹ, rõ dữ liệu, tránh hiệu ứng nặng.
- Token màu bắt buộc:
  - Primary: `#0EA5A5`
  - Primary Hover: `#0B8F8F`
  - Secondary: `#3B82F6`
  - Accent: `#10B981`
  - Page Background: `#F8FAFC`
  - Card Background: `#FFFFFF`
  - Main Text: `#0F172A`
  - Muted Text: `#475569`
  - Border: `#E2E8F0`
  - Success: `#16A34A`
  - Warning: `#D97706`
  - Danger: `#DC2626`
- Typography:
  - Heading: Be Vietnam Pro (600/700)
  - Body: Inter (400/500)
- Layout baseline:
  - Container max width: `1200px`
  - Radius: `12px`
  - Section spacing: `24px` mobile, `32px` desktop
  - Shadow: `0 4px 12px rgba(15, 23, 42, 0.06)`
- Không dùng bảng màu tím làm chủ đạo nếu không có yêu cầu đặc biệt.

## 12) Vietnamese Diacritics Rule

- Toàn bộ text tiếng Việt trên UI phải có dấu đầy đủ.
- Không dùng tiếng Việt không dấu cho label, thông báo, placeholder.
- Dữ liệu tiếng Việt do người dùng nhập phải giữ nguyên dấu.
