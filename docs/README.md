# Tài Liệu Hệ Thống Quản Lý Ký Túc Xá Sinh Viên

**Phiên bản:** 1.0  
**Cập nhật:** Tháng 3, 2026  
**Ngôn Ngữ:** Tiếng Việt

---

## 📚 Mục Lục Tài Liệu

Hệ thống này bao gồm **5 tài liệu chính** cho phát triển, vận hành và triển khai:

### 1. 📖 [API_DOCUMENTATION.md](./architecture/API_DOCUMENTATION.md)
**Tài liệu API đầy đủ - Bắt buộc đọc cho lập trình viên**

Bao gồm:
- Tổng quan hệ thống và kiến trúc
- Xác thực & phân quyền
- **50+ API endpoints** đầy đủ với request/response examples
- Entity & DTO structures
- Luồng business logic chính
- Quy tắc kinh doanh
- Lỗi & xử lý exception

**Thích hợp cho:**
- Backend developers (tích hợp API)
- Frontend developers (gọi API)
- QA testers (kiểm tra API)
- DevOps (triển khai endpoints)

---

### 2. 🎨 [FEATURES_AND_UI_WORKFLOWS.md](./workflows/FEATURES_AND_UI_WORKFLOWS.md)
**Tài liệu Features & UI - Hướng dẫn cho Frontend developers**

Bao gồm:
- Danh sách tất cả **19 pages** (Public, Student, Admin)
- Chi tiết từng page (components, luồng)
- **4 User Flows** chính
- Tính năng chính đã implement
- Cấu hình & thiết kế (colors, fonts, spacing)
- Component sizes & guidelines

**Thích hợp cho:**
- Frontend developers
- UI/UX designers
- Product managers
- QA (test user flows)

---

### 3. 🗄️ [DATABASE_SCHEMA.md](./architecture/DATABASE_SCHEMA.md)
**Tài liệu Database - Quan trọng cho DBAs & developers**

Bao gồm:
- Entity Relationships Diagram (ERD)
- **13 bảng chính** chi tiết (columns, types, constraints)
- Enums & data types
- Indexes & performance tuning
- Foreign keys & constraints
- Khởi tạo dữ liệu (DataInitializer)

**Thích hợp cho:**
- Database administrators
- Backend developers
- Data analysts
- System architects

---

### 4. 🚀 [DEPLOYMENT_AND_QUICK_REFERENCE.md](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md)
**Hướng dẫn Deployment & Quick Reference - Cho DevOps & SysAdmins**

Bao gồm:
- Quick reference (thông tin nhanh, URLs, demo accounts)
- Cách chạy locally từng bước
- **3 cách deployment** (Tomcat, Docker, Linux VPS)
- Troubleshooting & debugging (7 problem common)
- Common tasks (thêm dữ liệu, API endpoint mới, etc)
- Monitoring & maintenance

**Thích hợp cho:**
- DevOps engineers
- System administrators
- Deployment specialists
- IT support

---

### 5. 📋 [CODING_STANDARDS.md](./standards/CODING_STANDARDS.md) *(nếu tồn tại)*
**Tài liệu Chuẩn Code - Yêu cầu cho tất cả lập trình viên**

Bao gồm:
- Naming conventions
- API response format
- DTO-first rule
- Pagination rules
- UI consistency
- Vietnamese diacritics rule
- Error handling standards

**Thích hợp cho:**
- Tất cả developers
- Code reviewers
- QA engineers

---

## 🎯 Hướng Dẫn Nhanh Theo Vai Trò

### 👨‍💻 Backend Developer
**Bước 1:** Đọc [API_DOCUMENTATION.md](./architecture/API_DOCUMENTATION.md#kiến-trúc--công-nghệ) (Architecture)  
**Bước 2:** Đọc [DATABASE_SCHEMA.md](./architecture/DATABASE_SCHEMA.md) (Database entities)  
**Bước 3:** Đọc [DEPLOYMENT_AND_QUICK_REFERENCE.md](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#chạy-hệ-thống-locally) (Setup local)  
**Bước 4:** Đọc API endpoints từ [API_DOCUMENTATION.md#danh-sách-api-endpoints](./architecture/API_DOCUMENTATION.md#danh-sách-api-endpoints)  
**Bước 5:** Debug issues qua [Troubleshooting](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#troubleshooting--debugging)

---

### 🎨 Frontend Developer
**Bước 1:** Đọc [FEATURES_AND_UI_WORKFLOWS.md](./workflows/FEATURES_AND_UI_WORKFLOWS.md) (Pages overview)  
**Bước 2:** Đọc [FEATURES_AND_UI_WORKFLOWS.md#chi-tiết-từng-page](./workflows/FEATURES_AND_UI_WORKFLOWS.md#chi-tiết-từng-page) (Page details)  
**Bước 3:** Đọc [API_DOCUMENTATION.md#danh-sách-api-endpoints](./architecture/API_DOCUMENTATION.md#danh-sách-api-endpoints) (API endpoints)  
**Bước 4:** Đọc [FEATURES_AND_UI_WORKFLOWS.md#luồng-người-dùng-user-flows](./workflows/FEATURES_AND_UI_WORKFLOWS.md#luồng-người-dùng-user-flows) (User flows)  
**Bước 5:** Implement pages theo [Design tokens](./workflows/FEATURES_AND_UI_WORKFLOWS.md#cấu-hình--thiết-kế)

---

### 🗄️ Database Administrator
**Bước 1:** Đọc [DATABASE_SCHEMA.md#entity-relationships-diagram](./architecture/DATABASE_SCHEMA.md#entity-relationships-diagram) (ERD)  
**Bước 2:** Đọc [DATABASE_SCHEMA.md#chi-tiết-từng-bảng](./architecture/DATABASE_SCHEMA.md#chi-tiết-từng-bảng) (All tables)  
**Bước 3:** Đọc [DATABASE_SCHEMA.md#indexes--constraints](./architecture/DATABASE_SCHEMA.md#indexes--constraints) (Indexes & performance)  
**Bước 4:** Setup database qua [DEPLOYMENT_AND_QUICK_REFERENCE.md#bước-2-tạo-database](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#bước-2-tạo-database)

---

### 🚀 DevOps / Sys Admin
**Bước 1:** Đọc [DEPLOYMENT_AND_QUICK_REFERENCE.md#quick-reference--thông-tin-nhanh](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#quick-reference--thông-tin-nhanh)  
**Bước 2:** Đọc [DEPLOYMENT_AND_QUICK_REFERENCE.md#deployment-lên-server](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#deployment-lên-server)  
**Bước 3:** Đọc [DEPLOYMENT_AND_QUICK_REFERENCE.md#monitoring--maintenance](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#monitoring--maintenance)  
**Bước 4:** Setup theo deployment option (Tomcat/Docker/VPS)  
**Bước 5:** Monitor qua [Health Check Endpoint](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#health-check-endpoint)

---

### 🧪 QA / Tester
**Bước 1:** Đọc [FEATURES_AND_UI_WORKFLOWS.md#tính-năng-chính](./workflows/FEATURES_AND_UI_WORKFLOWS.md#tính-năng-chính) (Features)  
**Bước 2:** Đọc [FEATURES_AND_UI_WORKFLOWS.md#luồng-người-dùng-user-flows](./workflows/FEATURES_AND_UI_WORKFLOWS.md#luồng-người-dùng-user-flows) (User flows to test)  
**Bước 3:** Đọc [API_DOCUMENTATION.md#hướng-dẫn-sử-dụng](./architecture/API_DOCUMENTATION.md#hướng-dẫn-sử-dụng) (Testing guide)  
**Bước 4:** Đọc [DEPLOYMENT_AND_QUICK_REFERENCE.md#chạy-hệ-thống-locally](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#chạy-hệ-thống-locally) (Setup test environment)

---

### 👨‍💼 Product Manager
**Bước 1:** Đọc [API_DOCUMENTATION.md#tổng-quan-hệ-thống](./architecture/API_DOCUMENTATION.md#tổng-quan-hệ-thống) (Overview)  
**Bước 2:** Đọc [FEATURES_AND_UI_WORKFLOWS.md#danh-sách-pages](./workflows/FEATURES_AND_UI_WORKFLOWS.md#danh-sách-pages) (All pages)  
**Bước 3:** Đọc [API_DOCUMENTATION.md#quy-tắc-kinh-doanh](./architecture/API_DOCUMENTATION.md#quy-tắc-kinh-doanh) (Business rules)

---

## 📑 Index Nhanh - Tìm Thông Tin

### Tìm theo Chủ Đề

#### 🔐 Xác Thực & Phân Quyền
- [Xác thực & phân quyền](./architecture/API_DOCUMENTATION.md#xác-thực--phân-quyền)
- [Endpoints công khai](./architecture/API_DOCUMENTATION.md#xác-thực--phân-quyền)
- Demo accounts → [Quick Reference](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#demo-accounts)

#### 📝 Hợp Đồng (Contract Management)
- [Luồng hợp đồng](./architecture/API_DOCUMENTATION.md#luồng-business-logic-chính)
- [API contracts](./architecture/API_DOCUMENTATION.md#5-quản-lý-hợp-đồng---apiv1contracts)
- [Database contract table](./architecture/DATABASE_SCHEMA.md#9-contract-hợp-đồng)

#### 🏢 Quản Lý Tòa Nhà & Phòng
- [API buildings](./architecture/API_DOCUMENTATION.md#2-quản-lý-tòa-nhà---apiv1buildings)
- [API rooms](./architecture/API_DOCUMENTATION.md#3-quản-lý-phòng--giường---apiv1rooms)
- [Building/Room database](./architecture/DATABASE_SCHEMA.md#5-building-tòa-nhà)

#### 👥 Quản Lý Sinh Viên
- [API students](./architecture/API_DOCUMENTATION.md#4-quản-lý-sinh-viên---apiv1students)
- [Student database](./architecture/DATABASE_SCHEMA.md#2-student-sinh-viên)
- [Student management page](./workflows/FEATURES_AND_UI_WORKFLOWS.md#10-student-management-page-adminstudent_managementhtml)

#### 💰 Tính Toán Tiền & Giá Cọc
- [Quy tắc tiền cọc](./architecture/API_DOCUMENTATION.md#quy-tắc-kinh-doanh)
- [Contract pricing logic](./architecture/API_DOCUMENTATION.md#luồng-business-logic-chính)
- [ContractRequestDTO](./architecture/API_DOCUMENTATION.md#dto-contractrequestdto-admin-tạo)

#### 🧬 Giới Tính & Tương Thích
- [Gender compatibility rules](./architecture/API_DOCUMENTATION.md#2-giới-tính--phòng)
- [Gender normalization](./architecture/API_DOCUMENTATION.md#3-room--bed-management-page-adminbedshtml)
- [Database gender field](./architecture/DATABASE_SCHEMA.md#columns-5)

#### 🎨 UI & Frontend
- [All pages listed](./workflows/FEATURES_AND_UI_WORKFLOWS.md#danh-sách-pages)
- [Color scheme](./workflows/FEATURES_AND_UI_WORKFLOWS.md#color-theme)
- [Typography guidelines](./workflows/FEATURES_AND_UI_WORKFLOWS.md#typography)

#### 🗄️ Database & Schema
- [Entity diagram](./architecture/DATABASE_SCHEMA.md#entity-relationships-diagram)
- [All tables reference](./architecture/DATABASE_SCHEMA.md#chi-tiết-từng-bảng)
- [Enums & types](./architecture/DATABASE_SCHEMA.md#enums--types)

#### 🚀 Deployment & DevOps
- [Local setup](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#chạy-hệ-thống-locally)
- [Deploy Tomcat](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#cách-1-deployment-lên-tomcat)
- [Deploy Docker](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#cách-2-deployment-với-docker)
- [Deploy Linux VPS](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#cách-3-deployment-lên-linux-vps)

#### ⚠️ Troubleshooting
- [Common problems (7 items)](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#troubleshooting--debugging)
- [Port conflicts](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#problem-1-port-8080-đã-sử-dụng)
- [Database errors](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#problem-2-database-connection-failed)

---

## 📊 Thống Kê Hệ Thống

| Thông Tin | Số Lượng |
|-----------|---------|
| **Tables** | 13 |
| **Columns** | 80+ |
| **API Endpoints** | 50+ |
| **Pages** | 19 |
| **Business Flows** | 4 |
| **Roles** | 2 |
| **Entities** | 13 |

---

## 🔗 Links Nhanh

### 📖 Tài Liệu
- API Documentation: [API_DOCUMENTATION.md](./architecture/API_DOCUMENTATION.md)
- Features & UI: [FEATURES_AND_UI_WORKFLOWS.md](./workflows/FEATURES_AND_UI_WORKFLOWS.md)
- Database Schema: [DATABASE_SCHEMA.md](./architecture/DATABASE_SCHEMA.md)
- Deployment Guide: [DEPLOYMENT_AND_QUICK_REFERENCE.md](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md)

### 🔧 Cấu Hình
- Coding Standards: [CODING_STANDARDS.md](../standards/CODING_STANDARDS.md)
- Copilot Instructions: [.github/copilot-instructions.md](../.github/copilot-instructions.md)
- UI Quick Start: [UI_QUICK_START.md](./ui-ux/UI_QUICK_START.md)

### 📁 Source Code
- Controllers: `src/main/java/.../controller/`
- Services: `src/main/java/.../service/`
- Entities: `src/main/java/.../entity/`
- DTOs: `src/main/java/.../dto/`
- Repositories: `src/main/java/.../repository/`
- Frontend: `src/main/resources/static/`

---

## 🎓 Ví Dụ Tích Hợp

### Ví Dụ 1: Implement New API Endpoint

1. **Quyết định** logic / use case
2. **Đọc** [API_DOCUMENTATION.md#danh-sách-api-endpoints](./architecture/API_DOCUMENTATION.md#danh-sách-api-endpoints) để hiểu format
3. **Tạo** Controller method (follow standard pattern)
4. **Tạo** Service business logic
5. **Tạo** DTO request/response
6. **Test** theo [API examples](./architecture/API_DOCUMENTATION.md)
7. **Document** endpoint trong này file

---

### Ví Dụ 2: Implement New UI Page

1. **Đọc** [FEATURES_AND_UI_WORKFLOWS.md#danh-sách-pages](./workflows/FEATURES_AND_UI_WORKFLOWS.md#danh-sách-pages) để tìm page tương tự
2. **Hiểu** page requirements qua [Chi tiết từng page](./workflows/FEATURES_AND_UI_WORKFLOWS.md#chi-tiết-từng-page)
3. **Thiết kế** theo [cấu hình UI](./workflows/FEATURES_AND_UI_WORKFLOWS.md#cấu-hình--thiết-kế)
4. **Implement** HTML + Tailwind CSS
5. **Viết** JavaScript handlers
6. **Test** flow qua [User flows](./workflows/FEATURES_AND_UI_WORKFLOWS.md#luồng-người-dùng-user-flows)

---

### Ví Dụ 3: Add Database Table

1. **Thiết kế** table structure
2. **Kiểm tra** foreign keys theo [DATABASE_SCHEMA.md](./architecture/DATABASE_SCHEMA.md)
3. **Tạo** Entity class (extends BaseEntity)
4. **Tạo** Repository interface
5. **Tạo** Service & Controller
6. **Cập nhật** tài liệu database

---

## ✅ Checklist Khi Bắt Đầu

- [ ] Clone repository
- [ ] Đọc tài liệu phù hợp với role
- [ ] Setup environment (Java, SQL Server, IDE)
- [ ] Run locally theo [DEPLOYMENT_AND_QUICK_REFERENCE.md](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#bước-1-clone-repository)
- [ ] Kiểm tra demo accounts works
- [ ] Tạo branch feature từ develop
- [ ] Follow coding standards từ [CODING_STANDARDS.md](../standards/CODING_STANDARDS.md)
- [ ] Test trước khi commit
- [ ] Create PR với good description

---

## 📞 Support & Questions

**Troubleshooting:**
- Lỗi API? → Xem [API_DOCUMENTATION.md#lỗi--xử-lý-exception](./architecture/API_DOCUMENTATION.md#lỗi--xử-lý-exception)
- Lỗi UI? → Xem [FEATURES_AND_UI_WORKFLOWS.md](./workflows/FEATURES_AND_UI_WORKFLOWS.md)
- Lỗi Database? → Xem [DATABASE_SCHEMA.md](./architecture/DATABASE_SCHEMA.md)
- Lỗi Deploy? → Xem [DEPLOYMENT_AND_QUICK_REFERENCE.md#troubleshooting--debugging](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#troubleshooting--debugging)

**Cần nhanh?**
- Quick reference: [DEPLOYMENT_AND_QUICK_REFERENCE.md#quick-reference--thông-tin-nhanh](./operations/DEPLOYMENT_AND_QUICK_REFERENCE.md#quick-reference--thông-tin-nhanh)
- Demo accounts: Xem Quick Reference
- URLs: Xem Quick Reference

---

## 📝 Ghi Chú

- 🔁 Tất cả tài liệu được cập nhật cùng lúc
- 🇻🇳 Tất cả Vietnamese text sử dụng diacritics đầy đủ
- 📌 Follow [copilot-instructions.md](../.github/copilot-instructions.md) khi code
- 🎨 Follow design tokens từ [FEATURES_AND_UI_WORKFLOWS.md](./workflows/FEATURES_AND_UI_WORKFLOWS.md)
- ✅ Validate theo [CODING_STANDARDS.md](../standards/CODING_STANDARDS.md)

---

**Phát Triển Bởi:** Development Team  
**Phiên Bản:** 1.0  
**Cập Nhật Cuối:** Tháng 3, 2026

---

## 🎉 Chúc Mừng!

Bạn đã sẵn sàng bắt đầu phát triển hoặc triển khai hệ thống.  
Hãy chọn tài liệu phù hợp với vai trò của bạn từ **[Hướng Dẫn Nhanh Theo Vai Trò](#-hướng-dẫn-nhanh-theo-vai-trò)** ở trên.

**Chúc Bạn Thành Công! 🚀**

