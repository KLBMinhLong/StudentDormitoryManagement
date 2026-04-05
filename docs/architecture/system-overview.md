# Tổng Quan Hệ Thống

## 1) Mục tiêu nghiệp vụ

Hệ thống quản lý ký túc xá sinh viên cung cấp các nhóm chức năng chính:

- Quản lý tòa nhà, phòng, giường, loại phòng.
- Quản lý sinh viên, hồ sơ cá nhân, lịch sử nội trú.
- Vòng đời hợp đồng: giữ chỗ, nộp hồ sơ, duyệt/từ chối, thay đổi hợp đồng, hết hạn.
- Quản lý điện nước theo kỳ và sinh hóa đơn định kỳ.
- Thanh toán hóa đơn qua PayOS (tạo payment link, nhận webhook).
- Dashboard quản trị và xuất báo cáo Excel.

## 2) Kiến trúc logic

Hệ thống theo kiến trúc layered trong Spring Boot:

- `controller`: nhận request HTTP, kiểm tra input cơ bản, trả `ApiResponse<T>`.
- `service` + `service.impl`: xử lý nghiệp vụ, phân quyền nghiệp vụ, điều phối transaction.
- `repository`: truy vấn dữ liệu qua Spring Data JPA.
- `entity`: mô hình dữ liệu quan hệ trong PostgreSQL.
- `dto`: chuẩn dữ liệu vào/ra (request/response/paged).
- `security`: JWT filter, user details service, xử lý 401/403.
- `config`: bảo mật, cache, mapper, email, seed dữ liệu.

## 3) Công nghệ chính

- Java 21.
- Spring Boot 4.0.5.
- Spring Web MVC.
- Spring Security (JWT Bearer).
- Spring Data JPA + Hibernate.
- PostgreSQL.
- Caffeine cache.
- Apache POI (xuất Excel).
- PayOS REST API + webhook callback.

## 4) Cấu trúc endpoint

Base API: `/api/v1`

Nhóm endpoint chính:

- `auth`: đăng ký sinh viên, đăng nhập, quên/đặt lại mật khẩu, lấy thông tin hiện tại.
- `buildings`, `rooms`, `room-types`, `beds` (thông qua room).
- `students`: quản trị sinh viên và self-service profile.
- `contracts`: nghiệp vụ hợp đồng cho sinh viên và quản trị.
- `utility-records`: nhập/chốt/mở khóa chỉ số điện nước, timeline tiêu thụ.
- `invoices`: sinh hóa đơn, tìm kiếm, thanh toán, xác nhận thủ công.
- `payment`: webhook PayOS.
- `admin/dashboard`: số liệu tổng hợp cho quản trị.
- `reports/admin`: xuất báo cáo Excel.
- `pricing-policies`: chính sách giá dịch vụ.

## 5) Bảo mật và phân quyền

- Cơ chế xác thực: JWT Bearer token qua header `Authorization: Bearer <token>`.
- Vai trò chính: `ROLE_ADMIN`, `ROLE_STUDENT`.
- Quy tắc phân quyền:
  - Public: đăng nhập/đăng ký, trang tĩnh, webhook PayOS, xem một số dữ liệu công khai.
  - Student: profile cá nhân, hợp đồng của bản thân, hóa đơn của bản thân, timeline điện nước của bản thân.
  - Admin: quản trị tòa nhà/phòng/sinh viên/hợp đồng/chỉ số/hóa đơn/báo cáo.

## 6) Scheduler và tác vụ nền

- `ContractLifecycleScheduler` (mỗi 60 giây):
  - Hủy yêu cầu hợp đồng chờ đã quá hạn.
  - Hết hạn hợp đồng active đã quá ngày kết thúc và giải phóng giường.

- `InvoiceGenerationScheduler` (cron mặc định ngày 10 hàng tháng lúc 00:30):
  - Tự động sinh hóa đơn cho kỳ tháng trước dựa trên utility record đã chốt.

## 7) Caching

Cache Caffeine áp dụng cho dữ liệu đọc nhiều:

- `buildingsList`
- `buildingById`
- `roomTypesList`
- `pricingLatest`

Mục tiêu: giảm truy vấn lặp cho dữ liệu ít thay đổi.

## 8) Thành phần frontend

Frontend phục vụ dạng static từ `src/main/resources/static`:

- Khu vực người dùng: `static/user/*`.
- Khu vực quản trị: `static/admin/*`.
- Tài nguyên UI dùng chung: `static/ui/*`.

`HomeController` thực hiện forward/redirect route từ URL thân thiện sang file HTML tương ứng.

## 9) Điểm mở rộng khuyến nghị

- Bổ sung OpenAPI thực tế (hiện `SwaggerConfig` là placeholder).
- Tăng coverage test cho service/controller quan trọng.
- Tách cấu hình bí mật hoàn toàn sang secret manager.
- Bổ sung observability (log correlation id, metrics nghiệp vụ).