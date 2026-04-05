# API Contract Chuẩn Hóa

## 1) Định dạng phản hồi bắt buộc

Mọi API JSON phải trả theo cấu trúc:

```json
{
  "code": 200,
  "message": "Success",
  "result": {}
}
```

Trong đó:

- `code`: mã kết quả nghiệp vụ/HTTP tương ứng.
- `message`: thông điệp.
- `result`: dữ liệu trả về, có thể `null`.

## 2) Quy ước HTTP status

- GET/PUT thành công: `200`.
- POST tạo mới thành công: `201`.
- DELETE thành công: `204`, `result = null`.
- Lỗi validation/nghiệp vụ: `400`.
- Không tìm thấy: `404`.
- Lỗi hệ thống: `500`.

## 3) Phân trang chuẩn

Request query chuẩn:

- `page` mặc định `0`.
- `size` mặc định `10`.
- `sortBy` mặc định `id` hoặc `createdAt` theo ngữ cảnh.
- `direction` mặc định `desc`.

Response phân trang chuẩn trong `result`:

```json
{
  "content": [],
  "pageNo": 0,
  "pageSize": 10,
  "totalElements": 0,
  "totalPages": 0,
  "last": true
}
```

## 4) Chuẩn endpoint

- Prefix version: `/api/v1`.
- Tên resource dạng kebab-case.
- Ưu tiên danh từ số nhiều cho collection endpoint.

## 5) Chuẩn bảo mật

- JWT qua header `Authorization`.
- Endpoint protected phải kiểm tra role bằng `@PreAuthorize` hoặc security matcher.
- Webhook của bên thứ ba phải có cơ chế xác minh chữ ký và logging tối thiểu.

## 6) Chuẩn lỗi toàn cục

- Xử lý tập trung bằng `@RestControllerAdvice`.
- Body lỗi vẫn theo `ApiResponse<T>`.
- Không trả stacktrace ra response cho client production.

## 7) Quy tắc DTO

- Controller không expose entity trực tiếp.
- Input: `*RequestDTO`.
- Output: `*ResponseDTO` hoặc DTO nghiệp vụ phù hợp.
- Mapping thực hiện ở service layer.

## 8) Ví dụ thực thi

Ví dụ tạo mới thành công:

```json
{
  "code": 201,
  "message": "Tạo hợp đồng thành công",
  "result": {
    "id": 1001
  }
}
```

Ví dụ lỗi không tìm thấy:

```json
{
  "code": 404,
  "message": "Không tìm thấy hợp đồng",
  "result": null
}
```