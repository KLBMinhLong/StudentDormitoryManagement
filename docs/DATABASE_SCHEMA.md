# Tài Liệu Database Schema

**Phiên bản:** 1.0  
**Database:** Microsoft SQL Server  
**ORM:** Spring Data JPA (Hibernate)  

---

## Mục Lục

1. [Tổng Quan Schema](#tổng-quan-schema)
2. [Entity Relationships Diagram](#entity-relationships-diagram)
3. [Chi Tiết Từng Bảng](#chi-tiết-từng-bảng)
4. [Enums & Types](#enums--types)
5. [Indexes & Constraints](#indexes--constraints)
6. [Base Classes](#base-classes)

---

## Tổng Quan Schema

Hệ thống sử dụng **16 bảng chính** để lưu trữ dữ liệu:

```
Core Entities:
├── app_user (Tài khoản)
├── student (Sinh viên)
├── role (Va trò)

Facility Management:
├── building (Tòa nhà)
├── room (Phòng)
├── room_type (Loại phòng)
├── bed (Giường)

Contract Management:
├── contract (Hợp đồng)
├── contract_change_request (Yêu cầu thay đổi)

Finance & Operations:
├── invoice (Hóa đơn)
├── utility_record (Che Phí Dịch Vụ)
├── issue (Yêu Cầu Sửa Chữa)

Lookup Tables:
├── app_user_role (Many-to-Many)
```

---

## Entity Relationships Diagram

```
┌──────────────────┐
│     APP_USER     │
├──────────────────┤
│ id (PK)          │
│ username*        │
│ password         │
│ full_name        │
│ email            │
│ enabled          │
│ student_id (FK)──┐
│ created_at       │  ┌─────────────────┐
│ updated_at       │  │    STUDENT      │
└──────────────────┘  ├─────────────────┤
         │ M───1 M    │ id (PK)         │
         └────────────│ student_code*   │
                      │ full_name       │
    ┌─────────────────│ email           │
    │                │ phone           │
    │ 1───M          │ date_of_birth   │
    │                │ gender          │
    │                │ cccd*           │
    │                │ avatar          │
    │                │ created_at      │
    │                └─────────────────┘
    │                         │
    │                         │
    │                    1───M│
    │                         │
    │  ┌──────────────────────┴──────────────┐
    │  │                                     │
    │  ▼                                     ▼
┌──────────────────┐               ┌──────────────────┐
│    CONTRACT      │               │       BED        │
├──────────────────┤               ├──────────────────┤
│ id (PK)          │               │ id (PK)          │
│ student_id (FK)──┼──────────┐    │ bed_number       │
│ room_id (FK)─────┼──┐       │    │ room_id (FK)─────┼──┐
│ bed_id (FK)──────┼──┼───┐   │    │ is_occupied      │  │
│ start_date       │  │   │   │    │ student_id (FK)─────┤
│ end_date         │  │   │   │    │ reserved_until   │  │
│ deposit_amount   │  │   │   │    │ reserved_contract_id
│ monthly_room_price   │   │   │    │ created_at       │  │
│ total_room_amount    │   │   │    └──────────────────┘  │
│ duration_months  │  │   │   │            ▲              │
│ status (ENUM)    │  │   │   │            │              │
│ is_submitted     │  │   │   │            └──────────────┘
│ submitted_at     │  │   │   │
│ activated_at     │  │   │   │
│ hold_expires_at  │  │   │   │
│ emergency_contact_name   │    │
│ emergency_contact_phone  │    │
│ guardian_name    │  │   │   │
│ guardian_phone   │  │   │   │
│ student_note     │  │   │   │
│ created_at       │  │   │   │
│ updated_at       │  │   │   │
└──────────────────┘  │   │   │
                      │   │   │
           ┌──────────┘   │   │
           │              │   │
           ▼              │   ▼
    ┌──────────────────┐  │ ┌──────────────────┐
    │      ROOM        │  │ │    BUILDING      │
    ├──────────────────┤  │ ├──────────────────┤
    │ id (PK)          │  │ │ id (PK)          │
    │ room_number      │  │ │ name             │
    │ building_id (FK) │──┼─│ total_floors     │
    │ room_type_id(FK) │──┐│ description      │
    │ gender_allowed   │  ││ gender_allowed   │
    │ status (ENUM)    │  ││ created_at       │
    │ created_at       │  ││ updated_at       │
    │ updated_at       │  │└──────────────────┘
    └──────────────────┘  │
             ▲             │
             │             │
             └─────────────┘
                    │
                    ▼
    ┌──────────────────────┐
    │    ROOM_TYPE         │
    ├──────────────────────┤
    │ id (PK)              │
    │ name                 │
    │ capacity             │
    │ base_price           │
    │ amenities            │
    │ created_at           │
    │ updated_at           │
    └──────────────────────┘

┌──────────────────────────────────┐
│ CONTRACT_CHANGE_REQUEST          │
├──────────────────────────────────┤
│ id (PK)                          │
│ contract_id (FK)────────┬────────┤─→ CONTRACT
│ change_type (ENUM)      │        │
│ reason                  │        │
│ proposed_duration_months│        │
│ status (ENUM)           │        │
│ student_note            │        │
│ admin_note              │        │
│ created_at              │        │
│ approved_at             │        │
│ rejected_at             │        │
│ updated_at              │        │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│ INVOICE                          │
├──────────────────────────────────┤
│ id (PK)                          │
│ contract_id (FK)───────→ CONTRACT│
│ invoice_number                   │
│ invoice_date                     │
│ due_date                         │
│ amount_due                       │
│ amount_paid                      │
│ status (ENUM)                    │
│ description                      │
│ created_at                       │
│ updated_at                       │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│ UTILITY_RECORD                   │
├──────────────────────────────────┤
│ id (PK)                          │
│ room_id (FK)───────────→ ROOM    │
│ utility_type                     │
│ reading_date                     │
│ previous_reading                 │
│ current_reading                  │
│ usage_amount                     │
│ unit_price                       │
│ total_amount                     │
│ created_at                       │
│ updated_at                       │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│ ISSUE                            │
├──────────────────────────────────┤
│ id (PK)                          │
│ room_id (FK)───────────→ ROOM    │
│ reported_by_student_id (FK)      │
│ issue_title                      │
│ description                      │
│ priority (ENUM)                  │
│ status (ENUM)                    │
│ assigned_to (nvarchar)           │
│ created_at                       │
│ updated_at                       │
└──────────────────────────────────┘

┌────────────────────────────────┐
│ ROLE                           │
├────────────────────────────────┤
│ id (PK)                        │
│ role_name (ROLE_ADMIN, etc)    │
│ description                    │
│ created_at                     │
└────────────────────────────────┘

┌────────────────────────────────┐
│ APP_USER_ROLE (Many-to-Many)   │
├────────────────────────────────┤
│ user_id (FK, Composite PK)     │
│ role_id (FK, Composite PK)     │
└────────────────────────────────┘
```

---

## Chi Tiết Từng Bảng

### 1. APP_USER (Tài Khoản)

```sql
CREATE TABLE app_user (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(120),
    email NVARCHAR(254) UNIQUE,
    enabled BIT DEFAULT 1,
    student_id BIGINT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_app_user_student 
        FOREIGN KEY (student_id) REFERENCES student(id)
);
```

**Columns:**
| Tên | Kiểu | Ghi Chú |
|-----|------|---------|
| id | BIGINT | Primary Key, auto-increment |
| username | NVARCHAR(50) | Unique, dùng cho login |
| password | NVARCHAR(255) | Hashed (BCrypt) |
| full_name | NVARCHAR(120) | Tên đầy đủ người dùng |
| email | NVARCHAR(254) | Unique, email |
| enabled | BIT | Trạng thái tài khoản (0/1) |
| student_id | BIGINT | FK tới student (nullable) |
| created_at | DATETIME2 | Ngày tạo |
| updated_at | DATETIME2 | Ngày cập nhật |

**Indexes:**
- PK: id
- UK: username
- UK: email
- FK: student_id

---

### 2. STUDENT (Sinh Viên)

```sql
CREATE TABLE student (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    student_code NVARCHAR(20) NOT NULL UNIQUE,
    full_name NVARCHAR(120) NOT NULL,
    email NVARCHAR(254),
    phone NVARCHAR(20),
    date_of_birth DATE,
    gender NVARCHAR(10),  -- Nam, Nữ
    cccd NVARCHAR(20) NOT NULL UNIQUE,
    avatar NVARCHAR(255),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);
```

**Columns:**
| Tên | Kiểu | Ghi Chú |
|-----|------|---------|
| id | BIGINT | Primary Key |
| student_code | NVARCHAR(20) | Mã sinh viên, unique |
| full_name | NVARCHAR(120) | Tên đầy đủ |
| email | NVARCHAR(254) | Email |
| phone | NVARCHAR(20) | Số điện thoại |
| date_of_birth | DATE | Ngày sinh |
| gender | NVARCHAR(10) | Nam hoặc Nữ |
| cccd | NVARCHAR(20) | CCCD/CMND, unique |
| avatar | NVARCHAR(255) | URL avatar |
| created_at | DATETIME2 | Ngày tạo |
| updated_at | DATETIME2 | Ngày cập nhật |

---

### 3. ROLE (Vai Trò)

```sql
CREATE TABLE role (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    role_name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(255),
    created_at DATETIME2 DEFAULT GETDATE()
);
```

**Giá Trị:**
```
(1, 'ROLE_ADMIN', 'Administrator'),
(2, 'ROLE_STUDENT', 'Student')
```

---

### 4. APP_USER_ROLE (Many-to-Many)

```sql
CREATE TABLE app_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_app_user_role_user 
        FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_user_role_role 
        FOREIGN KEY (role_id) REFERENCES role(id)
);
```

---

### 5. BUILDING (Tòa Nhà)

```sql
CREATE TABLE building (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(120) NOT NULL,
    total_floors INT NOT NULL,
    description NVARCHAR(500),
    gender_allowed NVARCHAR(10),  -- Nam, Nữ
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);
```

**Columns:**
| Tên | Kiểu | Ghi Chú |
|-----|------|---------|
| id | BIGINT | Primary Key |
| name | NVARCHAR(120) | Tên tòa nhà |
| total_floors | INT | Tổng số tầng |
| description | NVARCHAR(500) | Mô tả |
| gender_allowed | NVARCHAR(10) | Giới tính: Nam/Nữ |
| created_at | DATETIME2 | Ngày tạo |
| updated_at | DATETIME2 | Ngày cập nhật |

---

### 6. ROOM_TYPE (Loại Phòng)

```sql
CREATE TABLE room_type (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    base_price DECIMAL(18, 2) NOT NULL,
    amenities NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);
```

**Ví Dụ Dữ Liệu:**
```
(1, 'Phòng 2 Người', 2, 1000000, 'Giường, Tủ quần áo')
(2, 'Phòng 4 Người', 4, 1500000, 'Giường, Tủ quần áo, Bàn học')
(3, 'Phòng 6 Người', 6, 2000000, 'Giường, Tủ quần áo, Bàn học, Tủ lạnh')
```

---

### 7. ROOM (Phòng)

```sql
CREATE TABLE room (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    room_number NVARCHAR(20) NOT NULL,
    building_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    gender_allowed NVARCHAR(10),  -- Nam, Nữ
    status NVARCHAR(20) DEFAULT 'AVAILABLE',  -- AVAILABLE, FULL, MAINTENANCE
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT uk_room_building_number 
        UNIQUE (building_id, room_number),
    CONSTRAINT fk_room_building 
        FOREIGN KEY (building_id) REFERENCES building(id),
    CONSTRAINT fk_room_room_type 
        FOREIGN KEY (room_type_id) REFERENCES room_type(id)
);
```

**Columns:**
| Tên | Kiểu | Ghi Chú |
|-----|------|---------|
| id | BIGINT | Primary Key |
| room_number | NVARCHAR(20) | Số phòng (e.g., A101) |
| building_id | BIGINT | FK tòa nhà |
| room_type_id | BIGINT | FK loại phòng |
| gender_allowed | NVARCHAR(10) | Nam/Nữ (thừa kế từ building) |
| status | NVARCHAR(20) | AVAILABLE/FULL/MAINTENANCE |
| created_at | DATETIME2 | Ngày tạo |
| updated_at | DATETIME2 | Ngày cập nhật |

**Constraints:**
- Unique (building_id, room_number)
- Foreign Key: building_id
- Foreign Key: room_type_id

---

### 8. BED (Giường)

```sql
CREATE TABLE bed (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    bed_number INT NOT NULL,
    room_id BIGINT NOT NULL,
    is_occupied BIT DEFAULT 0,
    student_id BIGINT,
    reserved_until DATETIME2,
    reserved_contract_id BIGINT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT uk_bed_room_number 
        UNIQUE (room_id, bed_number),
    CONSTRAINT fk_bed_room 
        FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    CONSTRAINT fk_bed_student 
        FOREIGN KEY (student_id) REFERENCES student(id)
);
```

**Columns:**
| Tên | Kiểu | Ghi Chú |
|-----|------|---------|
| id | BIGINT | Primary Key |
| bed_number | INT | Số giường trong phòng |
| room_id | BIGINT | FK phòng |
| is_occupied | BIT | Chiếm dụng? (0/1) |
| student_id | BIGINT | FK sinh viên (nếu occupied) |
| reserved_until | DATETIME2 | Hết hạn giữ chỗ |
| reserved_contract_id | BIGINT | ID hợp đồng giữ chỗ |
| created_at | DATETIME2 | Ngày tạo |
| updated_at | DATETIME2 | Ngày cập nhật |

---

### 9. CONTRACT (Hợp Đồng)

```sql
CREATE TABLE contract (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    student_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    duration_months INT NOT NULL,
    deposit_amount DECIMAL(18, 2) NOT NULL,
    monthly_room_price DECIMAL(18, 2) NOT NULL,
    total_room_amount DECIMAL(18, 2) NOT NULL,
    status NVARCHAR(20) DEFAULT 'PENDING',  -- PENDING, ACTIVE, EXPIRED, CANCELLED
    is_submitted BIT DEFAULT 0,
    submitted_at DATETIME2,
    activated_at DATETIME2,
    hold_expires_at DATETIME2,
    emergency_contact_name NVARCHAR(120),
    emergency_contact_phone NVARCHAR(20),
    guardian_name NVARCHAR(120),
    guardian_phone NVARCHAR(20),
    student_note NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_contract_student 
        FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_contract_room 
        FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT fk_contract_bed 
        FOREIGN KEY (bed_id) REFERENCES bed(id)
);
```

**Columns:**
| Tên | Kiểu | Ghi Chú |
|-----|------|---------|
| id | BIGINT | Primary Key |
| student_id | BIGINT | FK sinh viên |
| room_id | BIGINT | FK phòng |
| bed_id | BIGINT | FK giường |
| start_date | DATE | Ngày bắt đầu |
| end_date | DATE | Ngày kết thúc |
| duration_months | INT | Thời hạn (tháng) |
| deposit_amount | DECIMAL | Tiền cọc |
| monthly_room_price | DECIMAL | Giá phòng/tháng |
| total_room_amount | DECIMAL | Tổng tiền phòng |
| status | NVARCHAR(20) | PENDING/ACTIVE/EXPIRED/CANCELLED |
| is_submitted | BIT | Đã nộp hồ sơ? |
| submitted_at | DATETIME2 | Thời điểm nộp |
| activated_at | DATETIME2 | Thời điểm kích hoạt (approve) |
| hold_expires_at | DATETIME2 | Hết hạn giữ chỗ |
| emergency_contact_name | NVARCHAR(120) | Tên người liên hệ |
| emergency_contact_phone | NVARCHAR(20) | SĐT liên hệ |
| guardian_name | NVARCHAR(120) | Tên người giám hộ |
| guardian_phone | NVARCHAR(20) | SĐT person hộ |
| student_note | NVARCHAR(500) | Ghi chú sinh viên |
| created_at | DATETIME2 | Ngày tạo |
| updated_at | DATETIME2 | Ngày cập nhật |

---

### 10. CONTRACT_CHANGE_REQUEST (Yêu Cầu Thay Đổi)

```sql
CREATE TABLE contract_change_request (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    contract_id BIGINT NOT NULL,
    change_type NVARCHAR(50) NOT NULL,  -- EXTEND_CONTRACT, CHANGE_ROOM, EARLY_TERMINATION, OTHER
    reason NVARCHAR(500),
    proposed_duration_months INT,
    student_note NVARCHAR(500),
    admin_note NVARCHAR(500),
    status NVARCHAR(20) DEFAULT 'PENDING',  -- PENDING, APPROVED, REJECTED
    created_at DATETIME2 DEFAULT GETDATE(),
    approved_at DATETIME2,
    rejected_at DATETIME2,
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_ccr_contract 
        FOREIGN KEY (contract_id) REFERENCES contract(id)
);
```

---

### 11. INVOICE (Hóa Đơn)

```sql
CREATE TABLE invoice (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    contract_id BIGINT NOT NULL,
    invoice_number NVARCHAR(50) NOT NULL UNIQUE,
    invoice_date DATE NOT NULL,
    due_date DATE,
    amount_due DECIMAL(18, 2) NOT NULL,
    amount_paid DECIMAL(18, 2) DEFAULT 0,
    status NVARCHAR(20) DEFAULT 'UNPAID',  -- UNPAID, PAID, OVERDUE, CANCELLED
    description NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_invoice_contract 
        FOREIGN KEY (contract_id) REFERENCES contract(id)
);
```

---

### 12. UTILITY_RECORD (Chỉ Số Công Khai)

```sql
CREATE TABLE utility_record (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    room_id BIGINT NOT NULL,
    utility_type NVARCHAR(50),  -- Electricity, Water, Internet
    reading_date DATE NOT NULL,
    previous_reading DECIMAL(10, 2),
    current_reading DECIMAL(10, 2),
    usage_amount DECIMAL(10, 2),
    unit_price DECIMAL(18, 2),
    total_amount DECIMAL(18, 2),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_utility_record_room 
        FOREIGN KEY (room_id) REFERENCES room(id)
);
```

---

### 13. ISSUE (Yêu Cầu Sửa Chữa)

```sql
CREATE TABLE issue (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    room_id BIGINT NOT NULL,
    reported_by_student_id BIGINT,
    issue_title NVARCHAR(200) NOT NULL,
    description NVARCHAR(1000),
    priority NVARCHAR(20) DEFAULT 'MEDIUM',  -- LOW, MEDIUM, HIGH, URGENT
    status NVARCHAR(20) DEFAULT 'OPEN',  -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    assigned_to NVARCHAR(120),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    CONSTRAINT fk_issue_room 
        FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT fk_issue_student 
        FOREIGN KEY (reported_by_student_id) REFERENCES student(id)
);
```

---

## Enums & Types

### Contract Status
```sql
PENDING       - Chờ duyệt
ACTIVE        - Đang hoạt động
EXPIRED       - Hết hạn
CANCELLED     - Bị hủy
```

### Room Status
```sql
AVAILABLE     - Còn chỗ trống
FULL          - Đầy
MAINTENANCE   - Bảo trì
```

### Contract Change Request Status
```sql
PENDING       - Chờ xử lý
APPROVED      - Được phê duyệt
REJECTED      - Bị từ chối
```

### Contract Change Type
```sql
EXTEND_CONTRACT       - Gia hạn hợp đồng
CHANGE_ROOM           - Thay đổi phòng/giường
EARLY_TERMINATION     - Chấm dứt sớm
OTHER                 - Khác
```

### Invoice Status
```sql
UNPAID        - Chưa thanh toán
PAID          - Đã thanh toán
OVERDUE       - Quá hạn
CANCELLED     - Bị hủy
```

### Issue Priority
```sql
LOW           - Thấp
MEDIUM        - Trung bình
HIGH          - Cao
URGENT        - Khẩn cấp
```

### Issue Status
```sql
OPEN          - Mở
IN_PROGRESS   - Đang xử lý
RESOLVED      - Đã giải quyết
CLOSED        - Đã đóng
```

---

## Indexes & Constraints

### Unique Constraints
```sql
-- Username
ALTER TABLE app_user ADD CONSTRAINT uk_app_user_username UNIQUE (username);

-- Student Code
ALTER TABLE student ADD CONSTRAINT uk_student_code UNIQUE (student_code);

-- CCCD
ALTER TABLE student ADD CONSTRAINT uk_student_cccd UNIQUE (cccd);

-- Room Number within Building
ALTER TABLE room ADD CONSTRAINT uk_room_building_number UNIQUE (building_id, room_number);

-- Bed Number within Room
ALTER TABLE bed ADD CONSTRAINT uk_bed_room_number UNIQUE (room_id, bed_number);

-- Invoice Number
ALTER TABLE invoice ADD CONSTRAINT uk_invoice_number UNIQUE (invoice_number);
```

### Foreign Keys
```sql
-- App User → Student
ALTER TABLE app_user ADD CONSTRAINT fk_app_user_student 
    FOREIGN KEY (student_id) REFERENCES student(id);

-- Room → Building
ALTER TABLE room ADD CONSTRAINT fk_room_building 
    FOREIGN KEY (building_id) REFERENCES building(id);

-- Room → Room Type
ALTER TABLE room ADD CONSTRAINT fk_room_room_type 
    FOREIGN KEY (room_type_id) REFERENCES room_type(id);

-- Bed → Room
ALTER TABLE bed ADD CONSTRAINT fk_bed_room 
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE;

-- Bed → Student
ALTER TABLE bed ADD CONSTRAINT fk_bed_student 
    FOREIGN KEY (student_id) REFERENCES student(id);

-- Contract → Student
ALTER TABLE contract ADD CONSTRAINT fk_contract_student 
    FOREIGN KEY (student_id) REFERENCES student(id);

-- Contract → Room
ALTER TABLE contract ADD CONSTRAINT fk_contract_room 
    FOREIGN KEY (room_id) REFERENCES room(id);

-- Contract → Bed
ALTER TABLE contract ADD CONSTRAINT fk_contract_bed 
    FOREIGN KEY (bed_id) REFERENCES bed(id);

-- Contract Change Request → Contract
ALTER TABLE contract_change_request ADD CONSTRAINT fk_ccr_contract 
    FOREIGN KEY (contract_id) REFERENCES contract(id);

-- Invoice → Contract
ALTER TABLE invoice ADD CONSTRAINT fk_invoice_contract 
    FOREIGN KEY (contract_id) REFERENCES contract(id);

-- Utility Record → Room
ALTER TABLE utility_record ADD CONSTRAINT fk_utility_record_room 
    FOREIGN KEY (room_id) REFERENCES room(id);

-- Issue → Room
ALTER TABLE issue ADD CONSTRAINT fk_issue_room 
    FOREIGN KEY (room_id) REFERENCES room(id);

-- Issue → Student
ALTER TABLE issue ADD CONSTRAINT fk_issue_student 
    FOREIGN KEY (reported_by_student_id) REFERENCES student(id);

-- App User Role → App User
ALTER TABLE app_user_role ADD CONSTRAINT fk_app_user_role_user 
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE;

-- App User Role → Role
ALTER TABLE app_user_role ADD CONSTRAINT fk_app_user_role_role 
    FOREIGN KEY (role_id) REFERENCES role(id);
```

### Performance Indexes
```sql
-- Student lookup
CREATE INDEX idx_student_code ON student(student_code);
CREATE INDEX idx_student_cccd ON student(cccd);

-- Room lookup
CREATE INDEX idx_room_building_id ON room(building_id);
CREATE INDEX idx_room_status ON room(status);

-- Bed lookup
CREATE INDEX idx_bed_room_id ON bed(room_id);
CREATE INDEX idx_bed_student_id ON bed(student_id);

-- Contract lookup
CREATE INDEX idx_contract_student_id ON contract(student_id);
CREATE INDEX idx_contract_status ON contract(status);
CREATE INDEX idx_contract_start_date ON contract(start_date);
CREATE INDEX idx_contract_end_date ON contract(end_date);

-- Contract Change Request
CREATE INDEX idx_ccr_contract_id ON contract_change_request(contract_id);
CREATE INDEX idx_ccr_status ON contract_change_request(status);

-- Invoice lookup
CREATE INDEX idx_invoice_contract_id ON invoice(contract_id);
CREATE INDEX idx_invoice_status ON invoice(status);

-- Issue lookup
CREATE INDEX idx_issue_room_id ON issue(room_id);
CREATE INDEX idx_issue_status ON issue(status);
```

---

## Base Classes

### BaseEntity (Superclass)

```java
@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Kế Thừa Bởi:**
- AppUser
- Student
- Role
- Building
- Room
- RoomType
- Bed
- Contract
- ContractChangeRequest
- Invoice
- UtilityRecord
- Issue

---

## Data Initialization

### Khởi Tạo Dữ Liệu (DataInitializer)

```sql
-- Roles
INSERT INTO role (role_name, description) VALUES 
    ('ROLE_ADMIN', 'Administrator'),
    ('ROLE_STUDENT', 'Student');

-- Admin User
INSERT INTO app_user (username, password, full_name, email, enabled) VALUES 
    ('admin', '<hashed_password>', 'Admin User', 'admin@uni.edu.vn', 1);

-- Building
INSERT INTO building (name, total_floors, description, gender_allowed) VALUES 
    ('Tòa A', 5, 'Nam sinh viên năm 1-2', 'Nam'),
    ('Tòa B', 6, 'Nữ sinh viên năm 1-2', 'Nữ'),
    ('Tòa C', 4, 'Nam sinh viên năm 3-4', 'Nam');

-- Room Type
INSERT INTO room_type (name, capacity, base_price, amenities) VALUES 
    ('Phòng 2 Người', 2, 1000000, 'Giường, Tủ quần áo'),
    ('Phòng 4 Người', 4, 1500000, 'Giường, Tủ quần áo, Bàn học'),
    ('Phòng 6 Người', 6, 2000000, 'Giường, Tủ quần áo, Bàn học, Tủ lạnh');

-- Rooms & Beds → auto-created via application
```

---

**End of Database Documentation**
