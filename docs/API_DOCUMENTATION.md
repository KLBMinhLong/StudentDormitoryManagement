# Tài Liệu API - Hệ Thống Quản Lý Ký Túc Xá Sinh Viên

**Phiên bản:** 1.0  
**Cập nhật:** Tháng 3, 2026  
**Ngôn ngữ:** Vietnamese (Tiếng Việt)

---

## Mục Lục

1. [Tổng Quan Hệ Thống](#tổng-quan-hệ-thống)
2. [Kiến Trúc & Công Nghệ](#kiến-trúc--công-nghệ)
3. [Xác Thực & Phân Quyền](#xác-thực--phân-quyền)
4. [Định Dạng API Response](#định-dạng-api-response)
5. [Danh Sách API Endpoints](#danh-sách-api-endpoints)
6. [Các Entity & DTO](#các-entity--dto)
7. [Luồng Business Logic Chính](#luồng-business-logic-chính)
8. [Quy Tắc Kinh Doanh](#quy-tắc-kinh-doanh)
9. [Lỗi & Xử Lý Exception](#lỗi--xử-lý-exception)
10. [Hướng Dẫn Sử Dụng](#hướng-dẫn-sử-dụng)

---

## Tổng Quan Hệ Thống

Hệ thống Quản Lý Ký Túc Xá Sinh Viên là một ứng dụng web toàn vẹn hỗ trợ quản lý các nhân lực, tài sản và hoạt động liên quan đến ký túc xá, bao gồm:

### Chức Năng Chính
- **Quản lý Tòa Nhà** - Tạo, sửa xóa tòa nhà, kiểm soát giới tính
- **Quản lý Phòng & Giường** - Quản lý phòng, giường, trạng thái chiếm dụng
- **Quản lý Sinh Viên** - Hồ sơ sinh viên, thông tin cá nhân, upload avatar
- **Hệ Thống Hợp Đồng** - Ký hợp đồng, giữ chỗ giường, phê duyệt, yêu cầu thay đổi
- **Hóa Đơn & Cho Ưu Tiên** - Theo dõi công khai và khả năng cấp phát
- **Yêu Cầu Sửa Chữa** - Báo cáo sự cố, ưu tiên, trạng thái xử lý

### Người Dùng
- **Quản Trị Viên (ROLE_ADMIN)** - Quản lý toàn bộ hệ thống
- **Sinh Viên (ROLE_STUDENT)** - Đăng ký, quản lý hợp đồng cá nhân

---

## Kiến Trúc & Công Nghệ

### Backend Stack
```
Java 25 + Spring Boot 4.0.4
├── Spring Security (JWT-based)
├── Spring Data JPA (Hibernate ORM)
├── ModelMapper (DTO mapping)
├── Lombok (code generation)
└── Microsoft SQL Server (database)
```

### Frontend Stack
```
Vanilla JavaScript (ES6+)
├── Tailwind CSS (utility-first styling)
├── Fetch API (HTTP client)
├── HTML5 Semantic Markup
└── Be Vietnam Pro + Inter Fonts
```

### Kiến Trúc Phần Mềm
```
┌─────────────────────────────────────────┐
│         Frontend (HTML/JS/CSS)          │
└───────────────────┬─────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│        API Gateway / HomeController     │ (Routing)
└───────────────────┬─────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│      REST Controllers (API Layer)       │
│  ├── AuthController                     │
│  ├── StudentManagementController        │
│  ├── BuildingController                 │
│  ├── RoomController                     │
│  ├── ContractController                 │
│  └── RoomTypeController                 │
└───────────────────┬─────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│      Service Layer (Business Logic)     │
│  ├── StudentService                     │
│  ├── AuthService                        │
│  ├── BuildingService                    │
│  ├── RoomService                        │
│  ├── ContractService                    │
│  └── RoomTypeService                    │
└───────────────────┬─────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│    Repository Layer (Data Access)       │
│  ├── StudentRepository                  │
│  ├── BuildingRepository                 │
│  ├── RoomRepository                     │
│  ├── BedRepository                      │
│  ├── ContractRepository                 │
│  └── AppUserRepository                  │
└───────────────────┬─────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│  Microsoft SQL Server (Database)        │
└─────────────────────────────────────────┘
```

---

## Xác Thực & Phân Quyền

### Cơ Chế Xác Thực
- **Kiểu:** JWT (JSON Web Token)
- **Endpoint đăng nhập:** `POST /api/v1/auth/login`
- **Token lưu trữ:** LocalStorage (browser)
- **Header gửi token:** `Authorization: Bearer <token>`

### Vai Trò & Quyền Hạn
```
ROLE_ADMIN
├── Quản lý toàn bộ sinh viên
├── Tạo quản lý hợp đồng
├── Quản lý tòa nhà, phòng, giường
├── Phê duyệt hợp đồng
└── Xem/quản lý tất cả hợp đồng

ROLE_STUDENT
├── Xem hồ sơ cá nhân
├── Cập nhật hồ sơ cá nhân
├── Giữ chỗ giường (tạo reservation)
├── Nộp hồ sơ hợp đồng
├── Xem hợp đồng của mình
├── Tạo yêu cầu thay đổi hợp đồng
└── Xem yêu cầu thay đổi của mình
```

### Endpoints Công Khai (Không Cần Token)
- `GET /` - Trang chủ
- `GET /login` - Trang đăng nhập
- `GET /register` - Trang đăng ký
- `POST /api/v1/auth/register/student` - Đăng ký sinh viên mới
- `POST /api/v1/auth/login` - Đăng nhập
- `GET /api/v1/buildings` - Xem danh sách tòa nhà (không phân trang)
- `GET /api/v1/buildings/{id}` - Xem chi tiết tòa nhà
- `GET /api/v1/rooms` - Xem danh sách phòng (có phân trang)
- `GET /api/v1/room-types` - Xem danh sách loại phòng

---

## Định Dạng API Response

### Cấu Trúc Response Chuẩn
```json
{
  "code": 200,
  "message": "Success",
  "result": { /* dữ liệu hoặc null */ }
}
```

### Mã Lỗi Chuẩn
| Code | Ý Nghĩa |
|------|---------|
| 200 | Thành công (GET, PUT, DELETE có data) |
| 201 | Tạo mới thành công (POST) |
| 204 | Xóa thành công (DELETE, result = null) |
| 400 | Yêu cầu không hợp lệ |
| 401 | Chưa xác thực |
| 403 | Không có quyền hạn |
| 404 | Không tìm thấy |
| 500 | Lỗi máy chủ |

### Response Phân Trang
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "content": [ /* mảng dữ liệu */ ],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 50,
    "totalPages": 5,
    "last": false
  }
}
```

---

## Danh Sách API Endpoints

### 1. Xác Thực (Auth) - `/api/v1/auth`

#### 1.1 Đăng Ký Sinh Viên Mới
```http
POST /api/v1/auth/register/student
Content-Type: application/json

{
  "username": "sv001",
  "password": "password123",
  "fullName": "Nguyễn Văn A",
  "studentCode": "SV2024001",
  "cccd": "123456789",
  "email": "nguyenvana@uni.edu.vn",
  "phone": "0912345678",
  "dateOfBirth": "2005-01-15",
  "gender": "Nam"
}
```

**Response (201):**
```json
{
  "code": 201,
  "message": "Register student successfully",
  "result": {
    "id": 1,
    "username": "sv001",
    "fullName": "Nguyễn Văn A",
    "email": "nguyenvana@uni.edu.vn",
    "role": "ROLE_STUDENT"
  }
}
```

---

#### 1.2 Đăng Nhập
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "sv001",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Login successfully",
  "result": {
    "id": 1,
    "username": "sv001",
    "fullName": "Nguyễn Văn A",
    "email": "nguyenvana@uni.edu.vn",
    "role": "ROLE_STUDENT",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

#### 1.3 Lấy Thông Tin User Hiện Tại
```http
GET /api/v1/auth/me
Authorization: Bearer <token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Get current user successfully",
  "result": {
    "id": 1,
    "username": "sv001",
    "fullName": "Nguyễn Văn A",
    "role": "ROLE_STUDENT"
  }
}
```

---

### 2. Quản Lý Tòa Nhà - `/api/v1/buildings`

#### 2.1 Lấy Danh Sách Tòa Nhà
```http
GET /api/v1/buildings?page=0&size=10&sortBy=id&direction=desc
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Get all buildings successfully",
  "result": {
    "content": [
      {
        "id": 1,
        "name": "Tòa A",
        "totalFloors": 5,
        "description": "Tòa nhà dành cho nam sinh viên năm 1-2",
        "genderAllowed": "Nam",
        "createdAt": "2026-01-01T10:00:00"
      }
    ],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

#### 2.2 Lấy Chi Tiết Tòa Nhà
```http
GET /api/v1/buildings/1
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Get building successfully",
  "result": {
    "id": 1,
    "name": "Tòa A",
    "totalFloors": 5,
    "description": "Tòa nhà dành cho nam sinh viên năm 1-2",
    "genderAllowed": "Nam",
    "createdAt": "2026-01-01T10:00:00"
  }
}
```

---

#### 2.3 Tạo Tòa Nhà Mới (Admin)
```http
POST /api/v1/buildings
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Tòa B",
  "totalFloors": 6,
  "description": "Tòa nhà dành cho nữ sinh viên"
}
```

**Response (201):**
```json
{
  "code": 201,
  "message": "Create building successfully",
  "result": {
    "id": 2,
    "name": "Tòa B",
    "totalFloors": 6,
    "description": "Tòa nhà dành cho nữ sinh viên",
    "createdAt": "2026-03-30T15:00:00"
  }
}
```

---

#### 2.4 Cập Nhật Tòa Nhà (Admin)
```http
PUT /api/v1/buildings/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Tòa A - Cập Nhật",
  "totalFloors": 5,
  "description": "Tòa nhà dành cho nam sinh viên"
}
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Update building successfully",
  "result": { /* cấu trúc như Lấy Chi Tiết */ }
}
```

---

#### 2.5 Xóa Tòa Nhà (Admin)
```http
DELETE /api/v1/buildings/1
Authorization: Bearer <admin_token>
```

**Response (204):**
```json
{
  "code": 204,
  "message": "Delete building successfully",
  "result": null
}
```

---

### 3. Quản Lý Phòng & Giường - `/api/v1/rooms`

#### 3.1 Lấy Danh Sách Phòng (Có Bộ Lọc)
```http
GET /api/v1/rooms?keyword=A101&genderAllowed=Nam&buildingId=1&status=AVAILABLE&page=0&size=10&sortBy=id&direction=desc
```

**Query Parameters:**
- `keyword` - Tìm kiếm theo tên phòng
- `genderAllowed` - Lọc theo giới tính (Nam/Nữ)
- `buildingId` - Lọc theo ID tòa nhà
- `status` - Lọc theo trạng thái (AVAILABLE/FULL/MAINTENANCE)
- `page`, `size` - Phân trang
- `sortBy`, `direction` - Sắp xếp

**Response (200):**
```json
{
  "code": 200,
  "message": "Get all rooms successfully",
  "result": {
    "content": [
      {
        "id": 1,
        "roomNumber": "A101",
        "buildingId": 1,
        "buildingName": "Tòa A",
        "roomTypeId": 1,
        "roomTypeName": "Phòng 4 Người",
        "genderAllowed": "Nam",
        "status": "AVAILABLE",
        "beds": [
          {
            "id": 1,
            "bedNumber": 1,
            "isOccupied": false,
            "studentId": null
          }
        ],
        "createdAt": "2026-01-01T10:00:00"
      }
    ],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 20,
    "totalPages": 2,
    "last": false
  }
}
```

---

#### 3.2 Lấy Chi Tiết Phòng
```http
GET /api/v1/rooms/1
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Get room successfully",
  "result": {
    "id": 1,
    "roomNumber": "A101",
    "buildingId": 1,
    "buildingName": "Tòa A",
    "roomTypeId": 1,
    "roomTypeName": "Phòng 4 Người",
    "genderAllowed": "Nam",
    "status": "AVAILABLE",
    "beds": [
      {
        "id": 1,
        "bedNumber": 1,
        "isOccupied": false,
        "studentId": null
      },
      {
        "id": 2,
        "bedNumber": 2,
        "isOccupied": true,
        "studentId": 5
      }
    ]
  }
}
```

---

#### 3.3 Tạo Phòng Mới (Admin)
```http
POST /api/v1/rooms
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "roomNumber": "A102",
  "buildingId": 1,
  "roomTypeId": 1,
  "status": "AVAILABLE"
}
```

**Response (201):**
```json
{
  "code": 201,
  "message": "Create room successfully",
  "result": { /* cấu trúc như Lấy Chi Tiết */ }
}
```

---

#### 3.4 Cập Nhật Phòng (Admin)
```http
PUT /api/v1/rooms/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "roomNumber": "A102",
  "buildingId": 1,
  "roomTypeId": 1,
  "status": "MAINTENANCE"
}
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Update room successfully",
  "result": { /* cấu trúc như Lấy Chi Tiết */ }
}
```

---

#### 3.5 Xóa Phòng (Admin)
```http
DELETE /api/v1/rooms/1
Authorization: Bearer <admin_token>
```

**Response (204):**
```json
{
  "code": 204,
  "message": "Delete room successfully",
  "result": null
}
```

---

#### 3.6 Lấy Danh Sách Giường Trong Phòng
```http
GET /api/v1/rooms/1/beds?page=0&size=10&sortBy=bedNumber&direction=asc
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Get beds successfully",
  "result": {
    "content": [
      {
        "id": 1,
        "bedNumber": 1,
        "isOccupied": false,
        "studentId": null,
        "reservedUntil": null,
        "reservedContractId": null
      }
    ],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 4,
    "totalPages": 1,
    "last": true
  }
}
```

---

#### 3.7 Cập Nhật Chiếm Dụng Giường (Admin)
```http
PUT /api/v1/rooms/1/beds/1/occupancy
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "isOccupied": true,
  "studentId": 5
}
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Update bed occupancy successfully",
  "result": {
    "id": 1,
    "bedNumber": 1,
    "isOccupied": true,
    "studentId": 5
  }
}
```

---

#### 3.8 Lưu Bố Cục Giường (Admin)
```http
PUT /api/v1/rooms/1/beds/layout
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "beds": [
    {
      "bedNumber": 1,
      "isOccupied": false,
      "studentId": null
    },
    {
      "bedNumber": 2,
      "isOccupied": true,
      "studentId": 5
    }
  ]
}
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Save bed layout successfully",
  "result": { /* cấu trúc Room DTO */ }
}
```

---

### 4. Quản Lý Sinh Viên - `/api/v1/students`

#### 4.1 Lấy Danh Sách Sinh Viên (Admin)
```http
GET /api/v1/students?keyword=Nguyễn&page=0&size=10&sortBy=id&direction=desc
Authorization: Bearer <admin_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "content": [
      {
        "id": 1,
        "studentCode": "SV2024001",
        "fullName": "Nguyễn Văn A",
        "email": "nguyenvana@uni.edu.vn",
        "phone": "0912345678",
        "gender": "Nam",
        "cccd": "123456789",
        "dateOfBirth": "2005-01-15"
      }
    ],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 50,
    "totalPages": 5,
    "last": false
  }
}
```

---

#### 4.2 Lấy Chi Tiết Sinh Viên (Admin)
```http
GET /api/v1/students/1
Authorization: Bearer <admin_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "id": 1,
    "studentCode": "SV2024001",
    "fullName": "Nguyễn Văn A",
    "email": "nguyenvana@uni.edu.vn",
    "phone": "0912345678",
    "gender": "Nam",
    "cccd": "123456789",
    "dateOfBirth": "2005-01-15",
    "avatar": "/avatars/sv001.jpg",
    "createdAt": "2026-01-01T10:00:00"
  }
}
```

---

#### 4.3 Tạo Sinh Viên Mới (Admin)
```http
POST /api/v1/students
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "studentCode": "SV2024002",
  "fullName": "Trần Thị B",
  "email": "tranthib@uni.edu.vn",
  "phone": "0987654321",
  "gender": "Nữ",
  "cccd": "987654321",
  "dateOfBirth": "2005-05-20"
}
```

**Response (201):**
```json
{
  "code": 201,
  "message": "Created successfully",
  "result": { /* cấu trúc như Lấy Chi Tiết */ }
}
```

---

#### 4.4 Cập Nhật Sinh Viên (Admin)
```http
PUT /api/v1/students/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "studentCode": "SV2024001",
  "fullName": "Nguyễn Văn A - Cập Nhật",
  "email": "nguyenvana.new@uni.edu.vn",
  "phone": "0912345679",
  "gender": "Nam",
  "cccd": "123456789",
  "dateOfBirth": "2005-01-15"
}
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Updated successfully",
  "result": { /* cấu trúc như Lấy Chi Tiết */ }
}
```

---

#### 4.5 Xóa Sinh Viên (Admin)
```http
DELETE /api/v1/students/1
Authorization: Bearer <admin_token>
```

**Response (204):**
```json
{
  "code": 204,
  "message": "Deleted successfully",
  "result": null
}
```

---

#### 4.6 Lấy Hồ Sơ Cá Nhân (Student)
```http
GET /api/v1/students/me
Authorization: Bearer <student_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Success",
  "result": { /* cấu trúc như Lấy Chi Tiết */ }
}
```

---

#### 4.7 Cập Nhật Hồ Sơ Cá Nhân (Student)
```http
PUT /api/v1/students/me
Authorization: Bearer <student_token>
Content-Type: application/json

{
  "email": "newemail@uni.edu.vn",
  "phone": "0912345679"
}
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Updated successfully",
  "result": { /* cấu trúc như Lấy Chi Tiết */ }
}
```

---

#### 4.8 Thay Đổi Mật Khẩu (Student)
```http
PUT /api/v1/students/me/password
Authorization: Bearer <student_token>
Content-Type: application/json

{
  "oldPassword": "oldpassword123",
  "newPassword": "newpassword456"
}
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Password changed successfully",
  "result": null
}
```

---

#### 4.9 Upload Avatar (Admin)
```http
POST /api/v1/students/1/avatar
Authorization: Bearer <admin_token>
Content-Type: multipart/form-data

File: <image_file>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Upload successfully",
  "result": "/avatars/sv001.jpg"
}
```

---

#### 4.10 Upload Avatar Cá Nhân (Student)
```http
POST /api/v1/students/me/avatar
Authorization: Bearer <student_token>
Content-Type: multipart/form-data

File: <image_file>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Upload successfully",
  "result": "/avatars/sv001.jpg"
}
```

---

### 5. Quản Lý Hợp Đồng - `/api/v1/contracts`

#### 5.1 Tạo Hợp Đồng (Admin)
```http
POST /api/v1/contracts
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "studentId": 1,
  "roomId": 1,
  "bedId": 1,
  "durationMonths": 6
}
```

**Response (201):**
```json
{
  "code": 201,
  "message": "Tạo hợp đồng thành công",
  "result": {
    "id": 1,
    "studentId": 1,
    "studentCode": "SV2024001",
    "studentName": "Nguyễn Văn A",
    "roomId": 1,
    "roomNumber": "A101",
    "bedId": 1,
    "bedNumber": 1,
    "startDate": "2026-03-30",
    "endDate": "2026-08-29",
    "durationMonths": 6,
    "depositAmount": 1500000,
    "monthlyRoomPrice": 1500000,
    "totalRoomAmount": 9000000,
    "status": "ACTIVE",
    "submitted": true,
    "submittedAt": "2026-03-30T10:00:00",
    "activatedAt": "2026-03-30T10:00:00",
    "createdAt": "2026-03-30T10:00:00"
  }
}
```

---

#### 5.2 Giữ Chỗ Giường (Student - Step 1)
```http
POST /api/v1/contracts/reservations
Authorization: Bearer <student_token>
Content-Type: application/json

{
  "bedId": 1
}
```

**Response (201):**
```json
{
  "code": 201,
  "message": "Giữ chỗ giường thành công",
  "result": {
    "id": 2,
    "studentId": 2,
    "studentCode": "SV2024002",
    "roomId": 1,
    "roomNumber": "A101",
    "bedId": 1,
    "bedNumber": 1,
    "startDate": "2026-03-30",
    "endDate": "2026-09-28",
    "durationMonths": 6,
    "status": "PENDING",
    "submitted": false,
    "holdExpiresAt": "2026-03-30T10:10:00",
    "createdAt": "2026-03-30T10:00:00"
  }
}
```

**Ghi Chú:** Giữ chỗ có hiệu lực 10 phút. Nếu hết hạn, sinh viên phải giữ chỗ lại.

---

#### 5.3 Nộp Hồ Sơ Hợp Đồng (Student - Step 2)
```http
PUT /api/v1/contracts/2/submit
Authorization: Bearer <student_token>
Content-Type: application/json

{
  "durationMonths": 6,
  "emergencyContactName": "Trần Văn C",
  "emergencyContactPhone": "0912345678",
  "guardianName": "Trần Thị D",
  "guardianPhone": "0987654321",
  "studentNote": "Có nhu cầu sửa chữa phòng"
}
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Nộp hồ sơ hợp đồng thành công",
  "result": {
    "id": 2,
    "status": "PENDING",
    "submitted": true,
    "submittedAt": "2026-03-30T10:05:00",
    "holdExpiresAt": "2026-03-30T10:05:48",
    "emergencyContactName": "Trần Văn C",
    "emergencyContactPhone": "0912345678",
    "guardianName": "Trần Thị D",
    "guardianPhone": "0987654321",
    "studentNote": "Có nhu cầu sửa chữa phòng"
  }
}
```

---

#### 5.4 Lấy Hợp Đồng Chờ Duyệt Của Sinh Viên
```http
GET /api/v1/contracts/me/pending
Authorization: Bearer <student_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Lấy yêu cầu hợp đồng thành công",
  "result": { /* cấu trúc như Nộp Hồ Sơ */ }
}
```

---

#### 5.5 Lấy Danh Sách Hợp Đồng Của Sinh Viên
```http
GET /api/v1/contracts/me/contracts?page=0&size=10&sortBy=createdAt&direction=desc&status=ACTIVE&keyword=A101
Authorization: Bearer <student_token>
```

**Query Parameters:**
- `status` - Lọc theo trạng thái (PENDING/ACTIVE/EXPIRED/CANCELLED)
- `keyword` - Tìm kiếm theo tên phòng/giường
- `page`, `size` - Phân trang
- `sortBy`, `direction` - Sắp xếp

**Response (200):**
```json
{
  "code": 200,
  "message": "Lấy danh sách hợp đồng thành công",
  "result": {
    "content": [ /* danh sách hợp đồng */ ],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 5,
    "totalPages": 1,
    "last": true
  }
}
```

---

#### 5.6 Lấy Chi Tiết Hợp Đồng Của Sinh Viên
```http
GET /api/v1/contracts/me/contracts/2
Authorization: Bearer <student_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Lấy chi tiết hợp đồng thành công",
  "result": { /* đầy đủ thông tin hợp đồng */ }
}
```

---

#### 5.7 Phê Duyệt Hợp Đồng (Admin)
```http
PUT /api/v1/contracts/2/approve
Authorization: Bearer <admin_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Duyệt hợp đồng thành công",
  "result": {
    "id": 2,
    "status": "ACTIVE",
    "activatedAt": "2026-03-30T10:30:00",
    "startDate": "2026-03-30",
    "endDate": "2026-09-28"
  }
}
```

**Quy Tắc:**
- Độ dài hợp đồng được tính lại dựa trên thời điểm phê duyệt
- Ngày bắt đầu = ngày phê duyệt
- Độ dài = durationMonths từ hợp đồng

---

#### 5.8 Từ Chối Hợp Đồng (Admin)
```http
PUT /api/v1/contracts/2/reject?reason=Thông tin không chính xác
Authorization: Bearer <admin_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Từ chối hợp đồng thành công",
  "result": {
    "id": 2,
    "status": "CANCELLED",
    "cancelReason": "Thông tin không chính xác"
  }
}
```

---

#### 5.9 Lấy Danh Sách Hợp Đồng Chờ Duyệt (Admin)
```http
GET /api/v1/contracts/pending
Authorization: Bearer <admin_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Lấy danh sách chờ duyệt thành công",
  "result": [
    { /* danh sách hợp đồng PENDING */ }
  ]
}
```

---

#### 5.10 Lấy Danh Sách Hợp Đồng (Admin - Quản Lý)
```http
GET /api/v1/contracts/admin/management?page=0&size=10&status=ACTIVE&occupancyType=STAYED&keyword=A101
Authorization: Bearer <admin_token>
```

**Query Parameters:**
- `status` - Trạng thái hợp đồng
- `occupancyType` - Loại lưu trú (STAYED/NOT_STAYED)
- `keyword` - Tìm kiếm
- `page`, `size` - Phân trang

**Response (200):**
```json
{
  "code": 200,
  "message": "Lấy danh sách hợp đồng thành công",
  "result": { /* danh sách phân trang */ }
}
```

---

#### 5.11 Lấy Chi Tiết Hợp Đồng (Admin)
```http
GET /api/v1/contracts/admin/2
Authorization: Bearer <admin_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Lấy chi tiết hợp đồng thành công",
  "result": { /* đầy đủ thông tin */ }
}
```

---

#### 5.12 Hủy Hợp Đồng Sớm (Admin)
```http
PUT /api/v1/contracts/2/cancel-early?reason=Sinh viên xin rút khỏi
Authorization: Bearer <admin_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Hủy hợp đồng sớm thành công",
  "result": {
    "id": 2,
    "status": "CANCELLED",
    "cancelReason": "Sinh viên xin rút khỏi"
  }
}
```

---

#### 5.13 Tạo Yêu Cầu Thay Đổi Hợp Đồng (Student)
```http
POST /api/v1/contracts/2/change-requests
Authorization: Bearer <student_token>
Content-Type: application/json

{
  "changeType": "EXTEND_CONTRACT",
  "reason": "Muốn tiếp tục ở thêm 6 tháng",
  "proposedDurationMonths": 12,
  "studentNote": "Thời gian tiện"
}
```

**Change Types:**
- `EXTEND_CONTRACT` - Gia hạn hợp đồng
- `CHANGE_ROOM` - Thay đổi phòng/giường
- `EARLY_TERMINATION` - Chấm dứt sớm
- `OTHER` - Khác

**Response (201):**
```json
{
  "code": 201,
  "message": "Gửi yêu cầu thay đổi hợp đồng thành công",
  "result": {
    "id": 1,
    "contractId": 2,
    "changeType": "EXTEND_CONTRACT",
    "reason": "Muốn tiếp tục ở thêm 6 tháng",
    "status": "PENDING",
    "createdAt": "2026-03-30T10:30:00"
  }
}
```

---

#### 5.14 Lấy Danh Sách Yêu Cầu Thay Đổi Của Sinh Viên
```http
GET /api/v1/contracts/me/change-requests?page=0&size=10&status=PENDING
Authorization: Bearer <student_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Lấy danh sách yêu cầu thay đổi thành công",
  "result": { /* danh sách phân trang */ }
}
```

---

#### 5.15 Lấy Danh Sách Yêu Cầu Thay Đổi (Admin)
```http
GET /api/v1/contracts/admin/change-requests?page=0&size=10&status=PENDING&changeType=EXTEND_CONTRACT
Authorization: Bearer <admin_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Lấy danh sách yêu cầu thay đổi thành công",
  "result": { /* danh sách phân trang */ }
}
```

---

#### 5.16 Phê Duyệt Yêu Cầu Thay Đổi (Admin)
```http
PUT /api/v1/contracts/change-requests/1/approve?adminNote=Đồng ý gia hạn
Authorization: Bearer <admin_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Duyệt yêu cầu thay đổi thành công",
  "result": {
    "id": 1,
    "status": "APPROVED",
    "adminNote": "Đồng ý gia hạn",
    "approvedAt": "2026-03-30T11:00:00"
  }
}
```

---

#### 5.17 Từ Chối Yêu Cầu Thay Đổi (Admin)
```http
PUT /api/v1/contracts/change-requests/1/reject?adminNote=Không phê duyệt
Authorization: Bearer <admin_token>
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Từ chối yêu cầu thay đổi thành công",
  "result": {
    "id": 1,
    "status": "REJECTED",
    "adminNote": "Không phê duyệt",
    "rejectedAt": "2026-03-30T11:00:00"
  }
}
```

---

### 6. Loại Phòng - `/api/v1/room-types`

#### 6.1 Lấy Danh Sách Loại Phòng
```http
GET /api/v1/room-types?page=0&size=10&sortBy=id&direction=desc
```

**Response (200):**
```json
{
  "code": 200,
  "message": "Get all room types successfully",
  "result": {
    "content": [
      {
        "id": 1,
        "name": "Phòng 4 Người",
        "capacity": 4,
        "basePrice": 1500000,
        "amenities": "Giường, Tủ quần áo, Bàn học",
        "createdAt": "2026-01-01T10:00:00"
      }
    ],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 3,
    "totalPages": 1,
    "last": true
  }
}
```

---

## Các Entity & DTO

### Entity: Contract
```java
Entity: contract (bảng)
├── id (Long, PK)
├── student_id (Long, FK -> student)
├── room_id (Long, FK -> room)
├── bed_id (Long, FK -> bed)
├── start_date (LocalDate)
├── end_date (LocalDate)
├── duration_months (Integer)
├── deposit_amount (BigDecimal)
├── monthly_room_price (BigDecimal)
├── total_room_amount (BigDecimal)
├── status (Enum: PENDING, ACTIVE, EXPIRED, CANCELLED)
├── is_submitted (boolean)
├── submitted_at (LocalDateTime)
├── activated_at (LocalDateTime)
├── hold_expires_at (LocalDateTime)
├── emergency_contact_name (String, nvarchar(120))
├── emergency_contact_phone (String, nvarchar(20))
├── guardian_name (String, nvarchar(120))
├── guardian_phone (String, nvarchar(20))
├── student_note (String, nvarchar(500))
├── created_at (LocalDateTime)
└── updated_at (LocalDateTime)
```

### DTO: ContractResponseDTO
```json
{
  "id": 1,
  "studentId": 1,
  "studentCode": "SV2024001",
  "studentName": "Nguyễn Văn A",
  "roomId": 1,
  "roomNumber": "A101",
  "bedId": 1,
  "bedNumber": 1,
  "startDate": "2026-03-30",
  "endDate": "2026-09-28",
  "durationMonths": 6,
  "depositAmount": 1500000,
  "monthlyRoomPrice": 1500000,
  "totalRoomAmount": 9000000,
  "status": "ACTIVE",
  "submitted": true,
  "submittedAt": "2026-03-30T10:05:00",
  "activatedAt": "2026-03-30T10:30:00",
  "emergencyContactName": "Trần Văn C",
  "emergencyContactPhone": "0912345678",
  "guardianName": "Trần Thị D",
  "guardianPhone": "0987654321",
  "studentNote": "Có nhu cầu sửa chữa phòng",
  "createdAt": "2026-03-30T10:00:00",
  "updatedAt": "2026-03-30T10:30:00"
}
```

### DTO: ContractRequestDTO (Admin tạo)
```json
{
  "studentId": 1,
  "roomId": 1,
  "bedId": 1,
  "durationMonths": 6
}
```

### DTO: ContractSubmitRequestDTO (Student nộp)
```json
{
  "durationMonths": 6,
  "depositAmount": 1500000,
  "emergencyContactName": "Trần Văn C",
  "emergencyContactPhone": "0912345678",
  "guardianName": "Trần Thị D",
  "guardianPhone": "0987654321",
  "studentNote": "Có nhu cầu sửa chữa phòng"
}
```

### Entity: Student
```
Entity: student
├── id (Long, PK)
├── student_code (String, unique, nvarchar(20))
├── full_name (String, nvarchar(120))
├── email (String, nvarchar(254))
├── phone (String, nvarchar(20))
├── date_of_birth (LocalDate)
├── gender (String, nvarchar(10): Nam/Nữ)
├── cccd (String, unique, nvarchar(20))
├── avatar (String, nvarchar(255))
├── created_at (LocalDateTime)
└── updated_at (LocalDateTime)
```

### Entity: Room
```
Entity: room
├── id (Long, PK)
├── room_number (String, nvarchar(20))
├── building_id (Long, FK)
├── room_type_id (Long, FK)
├── gender_allowed (String: Nam/Nữ)
├── status (Enum: AVAILABLE, FULL, MAINTENANCE)
├── created_at (LocalDateTime)
└── updated_at (LocalDateTime)
```

### Entity: Bed
```
Entity: bed
├── id (Long, PK)
├── bed_number (Integer)
├── room_id (Long, FK)
├── is_occupied (boolean)
├── student_id (Long, FK -> student, nullable)
├── reserved_until (LocalDateTime, nullable)
├── reserved_contract_id (Long, nullable)
├── created_at (LocalDateTime)
└── updated_at (LocalDateTime)
```

### Entity: Building
```
Entity: building
├── id (Long, PK)
├── name (String, nvarchar(120))
├── total_floors (Integer)
├── description (String, nvarchar(500))
├── gender_allowed (String: Nam/Nữ)
├── created_at (LocalDateTime)
└── updated_at (LocalDateTime)
```

### Entity: AppUser (Tài Khoản)
```
Entity: app_user
├── id (Long, PK)
├── username (String, unique)
├── password (String, hashed)
├── full_name (String, nvarchar(120))
├── email (String, nvarchar(254))
├── enabled (boolean)
├── student_id (Long, FK -> student, nullable)
├── roles (Many-to-Many -> role)
├── created_at (LocalDateTime)
└── updated_at (LocalDateTime)
```

---

## Luồng Business Logic Chính

### 1. Luồng Đăng Ký Sinh Viên Mới

```
Bước 1: Sinh viên vào trang /register
        └─> Điền thông tin: username, password, fullName, studentCode, cccd, etc.

Bước 2: POST /api/v1/auth/register/student
        ├─ Validate thông tin (tất cả required)
        ├─ Kiểm tra duplicate: username, studentCode, cccd, email
        ├─ Kiểm tra gender hợp lệ (Nam/Nữ)
        └─ Nếu hợp lệ → tạo Student entity
                       → tạo AppUser với role ROLE_STUDENT
                       → mã hóa password

Bước 3: Trả về CurrentUserResponseDTO
        └─> Frontend lưu JWT token → Redirect /home

Lưu ý: Mỗi sinh viên được tạo account tự động với username = username đăng ký
```

---

### 2. Luồng Tạo Hợp Đồng (Admin)

```
Bước 1: Admin vào /admin/contracts → Click "Tạo Hợp Đồng Mới"

Bước 2: Admin điền Step 1 (Lựa chọn sinh viên)
        ├─ Tìm kiếm sinh viên theo mã đơn vị (code) hoặc tên
        ├─ GET /api/v1/students?keyword=...
        └─ Lựa chọn sinh viên

Bước 3: Admin điền Step 2 (Lựa chọn phòng & giường)
        ├─ Building (tùy chọn)
        ├─ GET /api/v1/rooms?genderAllowed=<sinh viên gender>&buildingId=?
        ├─ Lựa chọn phòng → Load giường trong phòng
        ├─ Kiểm tra giới tính giường (Nam/Nữ) phải khớp sinh viên
        └─ Lựa chọn giường trống

Bước 4: Admin điền Step 3 (Thời hạn & Tiền cọc)
        ├─ Chọn duration: 6 hoặc 12 tháng (dropdown)
        ├─ GET /api/v1/room-types (để lấy base price)
        ├─ Tiền cọc tự động = base price (1 tháng)
        ├─ Tính toán chi phí: total = base price × duration
        └─ Readonly fields: startDate, depositAmount

Bước 5: Admin submit
        └─> POST /api/v1/contracts
            {
              "studentId": 1,
              "roomId": 1,
              "bedId": 1,
              "durationMonths": 6
            }

Bước 6: Backend xử lý
        ├─ Validate gender match
        ├─ Check sinh viên không có hợp đồng mở khác
        ├─ Check giường có thể giữ chỗ
        ├─ Tính toán:
        │  ├─ startDate = LocalDate.now()
        │  ├─ endDate = startDate + 6/12 tháng - 1 ngày
        │  ├─ depositAmount = room.roomType.basePrice
        │  └─ totalAmount = depositAmount × durationMonths
        ├─ Tạo Contract (status = ACTIVE, submitted = true)
        ├─ Cập nhật Bed (isOccupied = true, student = this student)
        └─ Update Room status nếu cần

Bước 7: Admin xem kết quả
        └─> Success → Hiển thị danh sách hợp đồng
```

---

### 3. Luồng Giữ Chỗ & Nộp Hồ Sơ (Student - 2 Bước)

```
=== BƯỚC 1: GIỮNG CHỖ (Hold/Reserve) ===

Bước 1.1: Sinh viên vào /home → Xem danh sách phòng/tòa nhà
          ├─ GET /api/v1/buildings (danh sách tòa, không phân trang)
          ├─ GET /api/v1/rooms (danh sách phòng, phân trang)
          └─ Lọc theo: tòa nhà, giới tính tương thích

Bước 1.2: Sinh viên lựa chọn giường
          ├─ GET /api/v1/rooms/1 (xem chi tiết phòng/giường)
          ├─ Kiểm tra giường trống (isOccupied = false)
          └─ Kiểm tra giới tính tương thích

Bước 1.3: Sinh viên click "Giữ Chỗ"
          └─> POST /api/v1/contracts/reservations
              { "bedId": 1 }

Bước 1.4: Backend tạo contract PENDING
          ├─ Check sinh viên không có hợp đồng mở khác
          ├─ Check bed có thể giữ (không occupied)
          ├─ Tạo Contract:
          │  ├─ status = PENDING
          │  ├─ submitted = false
          │  ├─ startDate = today
          │  ├─ durationMonths = 6 (mặc định)
          │  ├─ holdExpiresAt = now + 10 phút
          │  └─ status còn PENDING xong nộp hồ sơ
          ├─ Cập nhật Bed:
          │  ├─ isOccupied = true
          │  ├─ student = null (chưa assign)
          │  ├─ reservedUntil = holdExpiresAt
          │  └─ reservedContractId = contractId
          └─> Response: ContractResponseDTO

Bước 1.5: Student xem màn hình nộp đơn
          └─> Trang "Nộp Hồ Sơ Hợp Đồng" (Step 2)

=== BƯỚC 2: NỘP HỒ SƠ (Submit Contract Profile) ===

Bước 2.1: Student điền thông tin bổ sung
          ├─ Thời hạn hợp đồng: 6 hoặc 12 tháng
          ├─ Tên người liên hệ khẩn cấp
          ├─ SĐT người liên hệ khẩn cấp
          ├─ Tên người giám hộ
          ├─ SĐT người giám hộ
          └─ Ghi chú (tùy chọn)

Lưu ý: Ngày bắt đầu/tiền cọc/giá tháng là READONLY
       (Tự động tính tại thời điểm phê duyệt)

Bước 2.2: Student submit
          └─> PUT /api/v1/contracts/{contractId}/submit
              {
                "durationMonths": 6,
                "depositAmount": 1500000,
                "emergencyContactName": "...",
                "emergencyContactPhone": "...",
                "guardianName": "...",
                "guardianPhone": "...",
                "studentNote": "..."
              }

Bước 2.3: Backend validate & update
          ├─ Check contract tồn tại & status = PENDING
          ├─ Check holdExpiresAt chưa hết (nếu hết → cancel)
          ├─ Validate durationMonths ∈ [6, 12]
          ├─ Update contract:
          │  ├─ submitted = true
          │  ├─ submittedAt = now
          │  ├─ durationMonths = from request
          │  ├─ startDate = today (tạm thời)
          │  ├─ endDate = today + durationMonths - 1
          │  ├─ depositAmount = room.roomType.basePrice
          │  ├─ totalRoomAmount = depositAmount × durationMonths
          │  ├─ emergencyContactName, guardianName, etc. từ request
          │  └─ holdExpiresAt = now + 48 giờ (thời gian chờ duyệt)
          └─> Response: ContractResponseDTO

Bước 2.4: Admin duyệt hợp đồng
          └─> PUT /api/v1/contracts/{contractId}/approve

Bước 2.5: Backend xử lý phê duyệt
          ├─ Check contract status = PENDING & submitted = true
          ├─ Recalculate ngày bắt đầu:
          │  ├─ approvedAt = now
          │  ├─ startDate = approvedAt.toLocalDate()
          │  └─ endDate = startDate + durationMonths - 1
          ├─ Update contract:
          │  ├─ status = ACTIVE
          │  ├─ activatedAt = now
          │  └─ holdExpiresAt = null (hết hiệu lực)
          ├─ Update Bed:
          │  ├─ isOccupied = true
          │  ├─ student = this student
          │  ├─ reservedUntil = null
          │  └─ reservedContractId = null
          └─> Response: Contract ACTIVE

Bước 2.6: Student xem kết quả
          ├─ GET /api/v1/contracts/me/contracts
          ├─ Thấy hợp đồng status = ACTIVE
          └─ Có thể xem chi tiết: GET /api/v1/contracts/me/contracts/{id}
```

---

### 4. Luồng Yêu Cầu Thay Đổi Hợp Đồng (Student)

```
Bước 1: Student xem hợp đồng hiện tại
        └─> GET /api/v1/contracts/me/contracts/{contractId}

Bước 2: Student click "Yêu Cầu Thay Đổi"
        ├─ Chọn loại thay đổi:
        │  ├─ EXTEND_CONTRACT (Gia hạn)
        │  ├─ CHANGE_ROOM (Đổi phòng)
        │  ├─ EARLY_TERMINATION (Chấm dứt sớm)
        │  └─ OTHER (Khác)
        ├─ Nhập lý do
        └─ Nhập ghi chú

Bước 3: Student submit
        └─> POST /api/v1/contracts/{contractId}/change-requests
            {
              "changeType": "EXTEND_CONTRACT",
              "reason": "...",
              "proposedDurationMonths": 12,
              "studentNote": "..."
            }

Bước 4: Backend tạo yêu cầu
        ├─ Validate contract tồn tại & thuộc sinh viên
        ├─ Tạo ContractChangeRequest:
        │  ├─ status = PENDING
        │  ├─ createdAt = now
        │  └─ approvedAt/rejectedAt = null
        └─> Response: ContractChangeRequestResponseDTO

Bước 5: Admin xem danh sách yêu cầu
        └─> GET /api/v1/contracts/admin/change-requests?status=PENDING

Bước 6: Admin phê duyệt hoặc từ chối
        ├─ Phê duyệt:
        │  └─> PUT /api/v1/contracts/change-requests/{id}/approve?adminNote=...
        │      ├─ Update request status = APPROVED
        │      ├─ Xử lý tuỳ theo changeType
        │      │  ├─ EXTEND: Update contract endDate
        │      │  ├─ CHANGE_ROOM: (future feature)
        │      │  └─ EARLY_TERMINATION: Set contract status = CANCELLED
        │      └─> Response: updated request
        │
        └─ Từ chối:
           └─> PUT /api/v1/contracts/change-requests/{id}/reject?adminNote=...
               ├─ Update request status = REJECTED
               └─> Response: updated request
```

---

## Quy Tắc Kinh Doanh

### 1. Hợp Đồng

| Quy Tắc | Mô Tả |
|---------|-------|
| **Thời Hạn** | Phải là 6 hoặc 12 tháng |
| **Tiền Cọc** | = Giá phòng loại đó (1 tháng) |
| **Ngày Bắt Đầu** | Được tính lại khi admin duyệt hợp đồng (= ngày duyệt) |
| **Ngày Kết Thúc** | = startDate + durationMonths - 1 ngày |
| **Giữ Chỗ** | Hiệu lực 10 phút (từ `holdExpiresAt`) |
| **Thời Gian Nộp Hồ Sơ** | Tối đa 48 giờ từ khi nộp đơn |
| **Một Sinh Viên** | Chỉ có 1 hợp đồng mở (PENDING hoặc ACTIVE) |

### 2. Giới Tính & Phòng

| Quy Tắc | Mô Tả |
|---------|-------|
| **Tòa Nhà** | Có genderAllowed (Nam/Nữ) |
| **Phòng** | Thừa kế genderAllowed từ tòa nhà |
| **Giường** | Thừa kế genderAllowed từ phòng |
| **Kiểm Tra** | Sinh viên giới tính phải == phòng genderAllowed |
| **Normalize** | "Nam", "male", "nam" → "Nam"; "Nữ", "female", "nữ" → "Nữ" |

### 3. Trạng Thái Hợp Đồng

```
PENDING  ──[admin phê duyệt]──> ACTIVE

PENDING  ──[hết hiệu lực/admin từ chối]──> CANCELLED

ACTIVE   ──[hạn hợp đồng]──> EXPIRED

ACTIVE   ──[admin hủy/student xin chấm dứt]──> CANCELLED
```

### 4. Phòng & Giường

| Trạng Thái | Mô Tả |
|-----------|-------|
| **AVAILABLE** | Còn giường trống |
| **FULL** | Tất cả giường đều occupied |
| **MAINTENANCE** | Phòng đang sửa chữa, không cho phép giữ chỗ |

---

## Lỗi & Xử Lý Exception

### Error Codes
```json
{
  "400": "Yêu cầu không hợp lệ - lỗi dữ liệu input",
  "401": "Chưa xác thực - thiếu token hoặc token không hợp lệ",
  "403": "Không có quyền hạn - user không có role cần thiết",
  "404": "Không tìm thấy - resource không tồn tại",
  "409": "Xung đột - dữ liệu đã tồn tại",
  "500": "Lỗi máy chủ - vấn đề nền tảng"
}
```

### Ví Dụ Error Response
```json
{
  "code": 400,
  "message": "Yêu cầu không hợp lệ",
  "result": null
}
```

### Lỗi Thường Gặp

| Lỗi | Nguyên Nhân | Giải Pháp |
|-----|-----------|----------|
| `Sinh viên chưa có hợp đồng` | Sinh viên không tìm thấy/không tồn tại | Kiểm tra ID sinh viên |
| `Giừơng đã đủ người` | Giường đã occupied | Chọn giường khác |
| `Quá thời gian giữ chỗ` | 10 phút hết hạn mà chưa nộp hồ sơ | Giữ chỗ lại và nộp ngay |
| `Giới tính không phù hợp` | Sinh viên nữ chọn phòng nam | Chọn phòng đúng giới tính |
| `Hợp đồng không sở hữu` | Student xem/sửa hợp đồng của người khác | Kiểm tra token & ID |

---

## Hướng Dẫn Sử Dụng

### Chạy Hệ Thống

#### Backend (Java Spring Boot)
```bash
# Build
./mvnw clean package -DskipTests

# Chạy trên port 8080
./mvnw spring-boot:run

# Hoặc build & chạy JAR
./mvnw clean package -DskipTests
java -jar target/management-0.0.1-SNAPSHOT.jar
```

#### Frontend
- Tất cả file HTML nằm trong `src/main/resources/static`
- Trình duyệt truy cập: `http://localhost:8080`
- Public pages: `/login`, `/register`, `/home`
- Admin pages: `/admin`, `/admin/students`, `/admin/contracts`
- Student pages: `/my-contracts`

### Tài Khoản Demo

| Loại | Username | Mật Khẩu | Ghi Chú |
|------|----------|----------|---------|
| Admin | admin | admin123 | Tạo từ DataInitializer |
| Student | SV2024001 | SV2024001 | Tạo từ đăng ký |

---

### Luồng Thử Nghiệm (Manual Testing)

#### 1. Đăng Ký Sinh Viên
1. Vào `/register`
2. Điền:Username, Password, Tên Đầy Đủ, Mã SV, CCCD, Email, SĐT, Ngày Sinh, Giới Tính
3. Click "Đăng Ký"
4. Kiểm tra: Redirect `/home` + JWT token trong localStorage

#### 2. Admin Tạo Hợp Đồng
1. Đăng nhập admin → `/admin/contracts`
2. Click "Tạo Hợp Đồng"
3. Step 1: Tìm sinh viên (bằng mã hoặc tên)
4. Step 2: Chọn tòa nhà (nếu cần) → Chọn phòng → Chọn giường
5. Step 3: Chọn 6 hoặc 12 tháng → Xem tiền cọc & tổng giá
6. Submit → Xem danh sách hợp đồng

#### 3. Student Giữ Chỗ & Nộp Hồ Sơ
1. Đăng nhập student → `/home`
2. Xem danh sách phòng → Click vào phòng
3. Chọn giường trống → Click "Giữ Chỗ"
4. Redirect `/user/contract-application`
5. Điền thông tin bổ sung + Chọn thời hạn
6. Submit → Chờ admin duyệt
7. Admin duyệt: `/admin/contracts` → "Duyệt Hợp Đồng"
8. Student xem hợp đồng ACTIVE

#### 4. Student Tạo Yêu Cầu Thay Đổi
1. Student xem hợp đồng → Click "Yêu Cầu Thay Đổi"
2. Chọn loại: EXTEND_CONTRACT, CHANGE_ROOM, EARLY_TERMINATION
3. Nhập lý do & ghi chú
4. Submit
5. Admin phê duyệt/từ chối

---

### Debugging & Troubleshooting

#### Token Không Hợp Lệ
```
Lỗi: "Unauthorized"
Giải pháp:
1. Xóa token cũ: localStorage.removeItem('accessToken')
2. Đăng nhập lại
3. Kiểm tra: Authorization: Bearer <token> header
```

#### CORS Error
```
Lỗi: "Access to fetch at ... has been blocked by CORS policy"
Giải pháp:
1. Backend đã config CORS trong SecurityConfig
2. Kiểm tra origin = http://localhost:8080
```

#### 500 Internal Server Error
```
Giải pháp:
1. Kiểm tra console backend (Terminal)
2. Xem error message → log file (logs/)
3. Kiểm tra database connection
4. Validate DTOs request body
```

---

## Phụ Lục

### A. Danh Sách Endpoints Theo Role

#### ROLE_ADMIN
```
POST   /api/v1/students                          - Tạo sinh viên
GET    /api/v1/students                          - Danh sách sinh viên
GET    /api/v1/students/{id}                     - Chi tiết sinh viên
PUT    /api/v1/students/{id}                     - Cập nhật sinh viên
DELETE /api/v1/students/{id}                     - Xóa sinh viên
POST   /api/v1/students/{id}/avatar              - Upload avatar admin
POST   /api/v1/contracts                         - Tạo hợp đồng
PUT    /api/v1/contracts/{contractId}/approve    - Phê duyệt hợp đồng
PUT    /api/v1/contracts/{contractId}/reject     - Từ chối hợp đồng
GET    /api/v1/contracts/pending                 - Hợp đồng chờ duyệt
GET    /api/v1/contracts/admin/management        - Danh sách hợp đồng
GET    /api/v1/contracts/admin/{contractId}      - Chi tiết hợp đồng
PUT    /api/v1/contracts/{contractId}/cancel-early - Hủy sớm
GET    /api/v1/contracts/admin/change-requests   - Yêu cầu thay đổi
PUT    /api/v1/contracts/change-requests/{id}/approve - Phê duyệt thay đổi
PUT    /api/v1/contracts/change-requests/{id}/reject  - Từ chối thay đổi
... (tất cả endpoints CRUD tòa nhà, phòng, loại phòng)
```

#### ROLE_STUDENT
```
GET    /api/v1/students/me                       - Hồ sơ cá nhân
PUT    /api/v1/students/me                       - Cập nhật hồ sơ
PUT    /api/v1/students/me/password              - Đổi mật khẩu
POST   /api/v1/students/me/avatar                - Upload avatar
POST   /api/v1/contracts/reservations            - Giữ chỗ (Step 1)
PUT    /api/v1/contracts/{contractId}/submit     - Nộp hồ sơ (Step 2)
GET    /api/v1/contracts/me/pending              - Hợp đồng chờ duyệt
GET    /api/v1/contracts/me/contracts            - Danh sách hợp đồng
GET    /api/v1/contracts/me/contracts/{id}       - Chi tiết hợp đồng
POST   /api/v1/contracts/{contractId}/change-requests - Tạo yêu cầu thay đổi
GET    /api/v1/contracts/me/change-requests      - Yêu cầu thay đổi của tôi
```

### B. Giá Trị Enum

```
ContractStatus: PENDING, ACTIVE, EXPIRED, CANCELLED
RoomStatus: AVAILABLE, FULL, MAINTENANCE
ContractChangeType: EXTEND_CONTRACT, CHANGE_ROOM, EARLY_TERMINATION, OTHER
ContractChangeRequestStatus: PENDING, APPROVED, REJECTED
Gender: Nam, Nữ
```

### C. JavaScript Examples

#### Login & Get Token
```javascript
async function login() {
  const response = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ 
      username: 'sv001', 
      password: 'password123' 
    })
  });
  
  const data = await response.json();
  if (data.code === 200) {
    localStorage.setItem('accessToken', data.result.accessToken);
    console.log('Đăng nhập thành công');
  }
}
```

#### API Call với Token
```javascript
async function fetchWithToken(endpoint, method = 'GET', body = null) {
  const token = localStorage.getItem('accessToken');
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    }
  };
  
  if (body) options.body = JSON.stringify(body);
  
  const response = await fetch(endpoint, options);
  return await response.json();
}
```

#### Lấy Danh Sách Sinh Viên
```javascript
async function getStudents() {
  const data = await fetchWithToken('/api/v1/students?page=0&size=10');
  console.log(data.result.content); // mảng sinh viên
}
```

---

**Chúc bạn sử dụng API thành công! 🎉**
