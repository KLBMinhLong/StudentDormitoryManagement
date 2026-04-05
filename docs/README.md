# Tài Liệu Dự Án Quản Lý Ký Túc Xá

Tài liệu này là mục lục chính cho toàn bộ dự án. Nội dung được viết theo trạng thái code hiện tại trong thư mục `src`.

## Cấu trúc tài liệu

- [architecture/README.md](architecture/README.md): Tổng quan kiến trúc hệ thống.
- [architecture/system-overview.md](architecture/system-overview.md): Luồng xử lý, lớp thành phần, tích hợp ngoài.
- [architecture/domain-model.md](architecture/domain-model.md): Mô hình dữ liệu nghiệp vụ.
- [architecture/api-catalog.md](architecture/api-catalog.md): Danh mục endpoint API theo controller.

- [deployment/README.md](deployment/README.md): Tổng quan triển khai.
- [deployment/run-guide.md](deployment/run-guide.md): Hướng dẫn chạy local, build và chạy production.
- [deployment/environment-variables.md](deployment/environment-variables.md): Danh mục biến môi trường.

- [operations/README.md](operations/README.md): Tổng quan vận hành.
- [operations/runbook.md](operations/runbook.md): Runbook theo tác vụ.
- [operations/troubleshooting.md](operations/troubleshooting.md): Cẩm nang xử lý sự cố.

- [standards/CODING_STANDARDS.md](standards/CODING_STANDARDS.md): Chuẩn code, chuẩn API, quy tắc DTO và phân trang.
- [standards/API_CONTRACT.md](standards/API_CONTRACT.md): Hợp đồng API chuẩn hóa.

- [ui-ux/UI_QUICK_START.md](ui-ux/UI_QUICK_START.md): Khởi động nhanh UI.
- [ui-ux/TAILWIND_THEME_SNIPPET.md](ui-ux/TAILWIND_THEME_SNIPPET.md): Snippet cấu hình Tailwind.
- [ui-ux/README.md](ui-ux/README.md): Hướng dẫn nhất quán giao diện.

- [workflows/README.md](workflows/README.md): Tổng quan quy trình làm việc.
- [workflows/development-workflow.md](workflows/development-workflow.md): Quy trình phát triển, review, release.
- [workflows/business-workflows-overview.md](workflows/business-workflows-overview.md): Tổng quan các luồng nghiệp vụ trọng yếu.
- [workflows/student-journey-workflow.md](workflows/student-journey-workflow.md): Hành trình nghiệp vụ sinh viên.
- [workflows/contract-lifecycle-workflow.md](workflows/contract-lifecycle-workflow.md): Vòng đời hợp đồng.
- [workflows/utility-invoice-workflow.md](workflows/utility-invoice-workflow.md): Luồng điện nước-hóa đơn.
- [workflows/payment-workflow.md](workflows/payment-workflow.md): Luồng thanh toán PayOS.

- [features-deep/README.md](features-deep/README.md): Phân tích sâu tính năng theo vị trí code.
- [features-deep/monthly-invoice-automation.md](features-deep/monthly-invoice-automation.md): Tự động tính hóa đơn từng tháng.
- [features-deep/bed-status-expiry-automation.md](features-deep/bed-status-expiry-automation.md): Tự động đổi trạng thái giường khi hết hạn chờ/hết hạn hợp đồng.

- [images/README.md](images/README.md): Hướng dẫn thêm ảnh màn hình cho README.

## Phạm vi hiện tại

- Backend: Spring Boot 4.0.5, Java 21, Spring Security, JPA, PostgreSQL.
- Frontend: Static HTML/CSS/JS phục vụ từ Spring Boot (`src/main/resources/static`).
- Xác thực: JWT Bearer.
- Tích hợp thanh toán: PayOS webhook và tạo liên kết thanh toán.

## Nguyên tắc cập nhật tài liệu

- Tài liệu phải đồng bộ với code thực tế.
- Khi thay đổi endpoint, profile, scheduler hoặc schema, cập nhật tài liệu tương ứng trong cùng pull request.
- Không đưa bí mật (mật khẩu, khóa API, token thật) vào tài liệu.