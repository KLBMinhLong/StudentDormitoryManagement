# Tài Liệu Features & UI Workflows

**Phiên bản:** 1.0  
**Cập nhật:** Tháng 3, 2026

---

## Mục Lục

1. [Danh Sách Pages](#danh-sách-pages)
2. [Chi Tiết Từng Page](#chi-tiết-từng-page)
3. [Luồng Người Dùng (User Flows)](#luồng-người-dùng-user-flows)
4. [Tính Năng Chính](#tính-năng-chính)
5. [Cấu Hình & Thiết Kế](#cấu-hình--thiết-kế)

---

## Danh Sách Pages

### Public Pages (Không Cần Đăng Nhập)
| Path | Tên | Chức Năng |
|------|-----|----------|
| `/` | Root Redirect | Chuyển hướng tới `/home` |
| `/login` | Đăng Nhập | Đăng nhập hệ thống |
| `/register` | Đăng Ký | Đăng ký tài khoản sinh viên mới |
| `/home` | Trang Chủ | Xem danh sách tòa nhà, phòng, giường trống |
| `/admin` | Admin Dashboard | Trang chính admin (nếu đăng nhập) |

### Student Pages (Cần ROLE_STUDENT)
| Path | Tên | Chức Năng |
|------|-----|----------|
| `/user/home.html` | Student Home | Xem danh sách tòa nhà, phòng, giường trống |
| `/user/student-profile.html` | Hồ Sơ Cá Nhân | Xem/cập nhật thông tin, đổi mật khẩu, upload avatar |
| `/user/contracts.html` | Danh Sách Hợp Đồng | Xem danh sách hợp đồng của sinh viên |
| `/user/contract-application.html` | Nộp Hồ Sơ Hợp Đồng | Nộp hồ sơ sau khi giữ chỗ (Step 2) |
| `/user/contract-guidance.html` | Hướng Dẫn Quy Trình | Hướng dẫn cách đăng ký giường & hợp đồng |
| `/my-contracts` | Redirect | Chuyển tới `/user/contracts.html` |

### Admin Pages (Cần ROLE_ADMIN)
| Path | Tên | Chức Năng |
|------|-----|----------|
| `/admin/index.html` | Admin Dashboard | Trang chính, thống kê, menu |
| `/admin/student_management.html` | Quản Lý Sinh Viên | CRUD sinh viên, tìm kiếm, upload avatar |
| `/admin/buildings.html` | Quản Lý Tòa Nhà | CRUD tòa nhà |
| `/admin/rooms.html` | Quản Lý Phòng | CRUD phòng, view giường, quản lý layout |
| `/admin/beds.html` | Quản Lý Giường | Cập nhật trạng thái giường, chiếm dụng |
| `/admin/contracts-management.html` | Quản Lý Hợp Đồng | Xem/duyệt/từ chối hợp đồng, quản lý yêu cầu thay đổi |
| `/admin/contract_registration.html` | Tạo Hợp Đồng | Form tạo hợp đồng mới (3 bước) |
| `/admin/contracts` | Redirect | Chuyển tới `/admin/contracts-management.html` |

---

## Chi Tiết Từng Page

### 1. Login Page (`/login`)

**Mục Đích:** Xác thực user

**Thành Phần:**
- Logo/Header
- Email/Username input
- Password input
- "Đăng Nhập" button
- "Quên mật khẩu?" link (future)
- "Đăng Ký" link → `/register`

**Luồng:**
1. User nhập username + password
2. Click "Đăng Nhập"
3. `POST /api/v1/auth/login`
4. Backend trả JWT token
5. Frontend lưu token vào localStorage
6. Redirect:
   - Admin → `/admin`
   - Student → `/user/home.html`

**Validation:**
- Username/Email: required, không trống
- Password: required, ≥ 6 ký tự

---

### 2. Register Page (`/register`)

**Mục Đích:** Đăng ký tài khoản sinh viên mới

**Thành Phần:**
- Logo/Header
- Form fields:
  - Username (unique, required)
  - Password (required, ≥ 6 ký tự)
  - Confirm Password (phải khớp)
  - Full Name (required)
  - Student Code (unique, required)
  - CCCD (unique, required)
  - Email (unique, optional)
  - Phone (optional)
  - Date of Birth (optional)
  - Gender (dropdown: Nam/Nữ, required)
- "Đăng Ký" button
- "Đã có tài khoản? Đăng nhập" link → `/login`

**Luộc:**
1. User điền toàn bộ thông tin
2. Validate client-side
3. Click "Đăng Ký"
4. `POST /api/v1/auth/register/student`
5. Backend tạo Student + AppUser
6. Trả JWT token
7. Redirect → `/user/home.html` (tự động đăng nhập)

**Validation:**
- Username: unique, 3-20 ký tự
- Password: ≥ 6 ký tự, có letter + number recommended
- Email: format email hợp lệ
- Student Code: unique, format XX-XXXX-XX
- CCCD: unique, 9-12 ký tự
- Gender: Nam hoặc Nữ

---

### 3. Home Page (`/home` hoặc `/user/home.html`)

**Mục Đích:** Xem danh sách tòa nhà, phòng có giường trống

**Thành Phần:**
- Header + Navigation
- Filter:
  - Building dropdown (all)
  - Gender filter (Nam/Nữ)
  - Search room number
- List/Card view:
  - Building name
  - Room number
  - Capacity
  - Available beds count
  - Room status badge
  - Monthly price
  - "Xem Chi Tiết" button
- Pagination (10 items/page)
- "Trang Chủ" / "Đăng Nhập" buttons (top right)

**Luồng:**
1. Load: `GET /api/v1/buildings`
2. Load: `GET /api/v1/rooms?page=0&size=10` (với filters)
3. User chọn filter → reload
4. Click "Xem Chi Tiết" → redirect `/room-detail.html?roomId=X`

**Ghi Chú:**
- Public page, không cần token
- Sinh viên chưa đăng nhập có thể xem
- Sau khi xem phòng, phải đăng nhập để giữ chỗ

---

### 4. Room Detail Page (`/room-detail.html?roomId=X`)

**Mục Đích:** Xem chi tiết phòng, giường trống, giữ chỗ

**Thành Phần:**
- Header
- Room info:
  - Tòa nhà
  - Số phòng
  - Loại phòng
  - Giá thuê/tháng
  - Mô tả
  - Gender allowed
  - Status
- Bed grid/list:
  - Bed number
  - Status icon (trống/đổ)
  - Student name (nếu occupied)
- "Giữ Chỗ" button (nếu có giường trống & student đăng nhập)
- Back link

**Luộc:**
1. Load: `GET /api/v1/rooms/{roomId}`
2. Render room info + beds
3. Student đăng nhập?
   - YES → Enable "Giữ Chỗ" button
   - NO → Show "Đăng nhập để giữ chỗ"
4. Click "Giữ Chỗ":
   - `POST /api/v1/contracts/reservations`
   - Redirect → `/user/contract-application.html?contractId=X`

---

### 5. Student Profile Page (`/user/student-profile.html`)

**Mục Đích:** Xem & cập nhật thông tin cá nhân

**Thành Phần:**
- Header + Sidebar navigation
- Current info display:
  - Student Code
  - Full Name
  - Email
  - Phone
  - Date of Birth
  - Gender
  - Avatar (with upload button)
- Edit form:
  - Email (editable)
  - Phone (editable)
  - Date of Birth (editable, optional)
  - Save button
- Change Password section:
  - Old Password
  - New Password
  - Confirm New Password
  - Change button
- Upload Avatar button (modal)

**Luộc:**
1. Load: `GET /api/v1/students/me` (get current student)
2. Render form với dữ liệu hiện tại
3. User edit → Click "Lưu"
4. `PUT /api/v1/students/me`
5. Toast: "Cập nhật thành công"
6. User click "Đổi Mật Khẩu":
   - `PUT /api/v1/students/me/password`
7. User upload avatar:
   - `POST /api/v1/students/me/avatar` (multipart)
   - Refresh avatar image

---

### 6. Contracts List Page (`/user/contracts.html`)

**Mục Đích:** Xem danh sách hợp đồng của sinh viên

**Thành Phần:**
- Header + Sidebar navigation
- Filter bar:
  - Status dropdown (All/PENDING/ACTIVE/EXPIRED/CANCELLED)
  - Search keyword (room number)
- Table/Card list:
  - Contract ID
  - Room
  - Bed
  - Status (badge: pending=yellow, active=green, etc.)
  - Start Date
  - End Date
  - Monthly Price
  - Total Amount
  - Action button: "Xem Chi Tiết"
- Pagination
- "Tạo Hợp Đồng Mới" button (nếu không có hợp đồng mở) → redirect `/user/home.html`

**Luộc:**
1. Load: `GET /api/v1/contracts/me/contracts?page=0&size=10&status=...`
2. Render list
3. User apply filter → reload
4. Click "Xem Chi Tiết" → modal hoặc redirect `/user/contracts.html?contractId=X`

---

### 7. Contract Application Page (`/user/contract-application.html`)

**Mục Đích:** Nộp hồ sơ hợp đồng sau khi giữ chỗ (Step 2)

**Thành Phần:**
- Header + Breadcrumb: "Giữ Chỗ > Nộp Hồ Sơ"
- Contract summary (readonly):
  - Room
  - Bed
  - Duration (6/12 tháng, readonly)
  - Monthly Price (readonly, auto from room type)
  - Total Amount = Monthly × Duration (readonly)
  - Start Date: readonly text "Tự động tính từ thời điểm ban quản lý duyệt"
- Form fields:
  - Duration dropdown: 6 hoặc 12 tháng (required)
  - Emergency Contact Name (required)
  - Emergency Contact Phone (required, format)
  - Guardian Name (required)
  - Guardian Phone (required, format)
  - Student Note (optional, textarea)
- Countdown timer: Hiển thị thời gian còn lại để nộp (holdExpiresAt)
- "Nộp Hồ Sơ" button (disabled nếu hết hạn)
- "Hủy" button (cancel reservation)

**Luộc:**
1. Load: `GET /api/v1/contracts/me/pending` (get contract being filled)
2. Render form
3. User fill info + select duration
4. Monthly price auto-calc từ API room type
5. Click "Nộp Hồ Sơ":
   - `PUT /api/v1/contracts/{contractId}/submit`
   - Toast: "Nộp hồ sơ thành công"
   - Redirect → `/user/contracts.html`
6. Countdown timer hits 0:
   - Toast: "Hết thời gian giữ chỗ, vui lòng chọn lại"
   - Disable form
7. Click "Hủy":
   - `DELETE /api/v1/contracts/{contractId}` (nếu có endpoint)
   - Redirect → `/user/home.html`

---

### 8. Contract Guidance Page (`/user/contract-guidance.html`)

**Mục Đích:** Hướng dẫn quy trình đăng ký giường & hợp đồng

**Thành Phần:**
- Header
- Steps infographics:
  ```
  Bước 1: Xem & Chọn Giường
  ↓
  Bước 2: Giữ Chỗ (10 phút)
  ↓
  Bước 3: Nộp Gồ Sơ (48 giờ)
  ↓
  Bước 4: Chờ Admin Duyệt
  ↓
  Bước 5: Hợp Đồng Kích Hoạt
  ```
- Chi tiết mỗi bước:
  - Mô tả
  - Requirements
  - Thời gian
  - Lưu ý
- FAQ section
- Contact support link

---

### 9. Admin Dashboard (`/admin/index.html`)

**Mục Đích:** Trang chính admin, overview & quick links

**Thành Phần:**
- Header + Sidebar menu:
  - Dashboard (current)
  - Student Management
  - Building Management
  - Room Management
  - Contract Management
  - Reports (future)
  - Settings (future)
- Widgets/Cards:
  - Total Students
  - Total Rooms
  - Occupied Beds count
  - Pending Contracts count
  - Recent Activity
  - Quick Actions

**Luộc:**
1. Load dashboard
2. Fetch overview stats từ various endpoints
3. Render cards
4. Click quick action → navigate tới page tương ứng

---

### 10. Student Management Page (`/admin/student_management.html`)

**Mục Đích:** Quản lý hồ sơ sinh viên (CRUD)

**Thành Phần:**
- Search bar:
  - Filter by name/student code
  - Search button
- Table:
  - Student Code
  - Full Name
  - Email
  - Phone
  - Gender
  - CCCD
  - Avatar
  - Actions: Edit, Delete, Upload Avatar
- Pagination
- "Thêm Sinh Viên" button → modal form
- Edit/Add modal:
  - Student Code (readonly if edit)
  - Full Name
  - Email
  - Phone
  - Gender
  - CCCD
  - Date of Birth
  - Save button

**Luộc (CRUD):**

**Create:**
1. Click "Thêm Sinh Viên"
2. Open modal
3. Form fields (all required except optional fields)
4. Click "Lưu"
5. `POST /api/v1/students`
6. Toast: "Tạo sinh viên thành công"
7. Reload table

**Read:**
1. Load: `GET /api/v1/students?keyword=...&page=...`
2. Render list

**Update:**
1. Click "Sửa" icon
2. Modal pre-fill data
3. Edit fields
4. Click "Lưu"
5. `PUT /api/v1/students/{id}`
6. Toast: "Cập nhật thành công"
7. Reload table

**Delete:**
1. Click "Xóa" icon
2. Confirm dialog
3. `DELETE /api/v1/students/{id}`
4. Toast: "Xóa thành công"
5. Reload table

**Upload Avatar:**
1. Click avatar or upload button
2. File picker
3. `POST /api/v1/students/{id}/avatar`
4. Update avatar display

---

### 11. Buildings Management Page (`/admin/buildings.html`)

**Mục Đích:** Quản lý tòa nhà

**Thành Phần:**
- Search bar
- Table:
  - Building Name
  - Total Floors
  - Description
  - Gender Allowed
  - Rooms Count
  - Occupied Beds
  - Actions: Edit, Delete, View Rooms
- "Thêm Tòa Nhà" button
- Edit modal: (tương tự Student Management)

**Luộc:**
1. Load: `GET /api/v1/buildings?page=...`
2. CRUD operations tương tự Student Management

---

### 12. Rooms Management Page (`/admin/rooms.html`)

**Mục Đích:** Quản lý phòng

**Thành Phần:**
- Filter:
  - Building dropdown
  - Room Type dropdown
  - Status dropdown
  - Gender filter
  - Search
- Table:
  - Room Number
  - Building
  - Room Type
  - Capacity
  - Occupied/Total Beds
  - Status (badge)
  - Gender Allowed
  - Actions: Edit, Delete, Manage Beds, View Layout
- "Thêm Phòng" button
- Edit modal

**Luộc:**
1. Load: `GET /api/v1/rooms?building=...&status=...&page=...`
2. CRUD operations
3. "Quản Lý Giường" → modal/page để view/edit beds
4. "Xem Bố Cục" → visual grid layout of beds

---

### 13. Beds Management Page (`/admin/beds.html`)

**Mục Đích:** Quản lý giường & chiếm dụng

**Thành Phần:**
- Filter:
  - Building, Room, Bed status
  - Search
- Table/Grid:
  - Bed Number
  - Room
  - Is Occupied
  - Student Name (nếu occupied)
  - Reserved Until (nếu reserved)
  - Actions: Edit, Release
- "Cập Nhật Bố Cục" button → layout designer

**Luộc:**
1. Load: `GET /api/v1/rooms/{roomId}/beds?page=...`
2. Edit bed: Update isOccupied, studentId
   - `PUT /api/v1/rooms/{roomId}/beds/{bedId}/occupancy`
3. Release bed → set isOccupied=false, studentId=null

---

### 14. Contract Management Page (`/admin/contracts-management.html`)

**Mục Đích:** Quản lý hợp đồng, duyệt, yêu cầu thay đổi

**Thành Phần:**

**Tab 1: Danh Sách Hợp Đồng**
- Filter:
  - Status (All/PENDING/ACTIVE/EXPIRED/CANCELLED)
  - Occupancy Type (All/STAYED/NOT_STAYED)
  - Keyword
  - Date range (date picker)
- Table:
  - Contract ID
  - Student Code/Name
  - Room/Bed
  - Start - End Date
  - Duration
  - Status (badge color)
  - Actions: View, Approve, Reject, Cancel Early, Edit

**Tab 2: Yêu Cầu Thay Đổi**
- Filter:
  - Status (All/PENDING/APPROVED/REJECTED)
  - Change Type (EXTEND/CHANGE_ROOM/EARLY_TERMINATION/OTHER)
  - Keyword
- Table:
  - Request ID
  - Student
  - Contract
  - Change Type
  - Reason
  - Status
  - Actions: Approve, Reject, View Details

**Luộc - Tab 1:**
1. Load: `GET /api/v1/contracts/admin/management?status=...&occupancyType=...&page=...`
2. Click "Chi Tiết":
   - Modal/page: full contract info
3. Click "Duyệt" (status=PENDING):
   - `PUT /api/v1/contracts/{id}/approve`
   - Toast: "Duyệt hợp đồng thành công"
   - Update status → ACTIVE
4. Click "Từ Chối" (status=PENDING):
   - Modal: input reject reason
   - `PUT /api/v1/contracts/{id}/reject?reason=...`
   - Toast: "Từ chối thành công"
   - Update status → CANCELLED
5. Click "Hủy Sớm" (status=ACTIVE):
   - Modal: input cancel reason
   - `PUT /api/v1/contracts/{id}/cancel-early?reason=...`
   - Toast: "Hủy hợp đồng thành công"
   - Update status → CANCELLED

**Luộc - Tab 2:**
1. Load: `GET /api/v1/contracts/admin/change-requests?status=...&changeType=...&page=...`
2. Click "Duyệt":
   - Modal: input admin note (optional)
   - `PUT /api/v1/contracts/change-requests/{id}/approve?adminNote=...`
   - Toast: "Duyệt yêu cầu thành công"
   - If EXTEND → update contract endDate
3. Click "Từ Chối":
   - Modal: input admin note (optional)
   - `PUT /api/v1/contracts/change-requests/{id}/reject?adminNote=...`
   - Toast: "Từ chối yêu cầu thành công"

---

### 15. Contract Registration Page (`/admin/contract_registration.html`)

**Mục Đích:** Tạo hợp đồng mới (admin tạo trực tiếp cho sinh viên)

**Thành Phần: 3-Step Form**

**Step 1: Chọn Sinh Viên**
- Search input (by code or name)
- Results list:
  - Student Code
  - Full Name
  - Gender
  - Email
  - Select button
- Load current contracts indicator

**Step 2: Chọn Phòng & Giường**
- Building dropdown (optional):
  - "Tất cả tòa nhà" (default)
  - Admin có thể bỏ qua bước này
- After selecting building/student gender:
  - Preload rooms → `GET /api/v1/rooms?genderAllowed=<student gender>&buildingId=...`
  - Show friendly message: "Các phòng phù hợp với giới tính sinh viên"
  - Rooms auto-load sau khi chọn sinh viên
- Room dropdown:
  - Filter tự động theo gender sinh viên & building (nếu chọn)
  - Show: Room Number, Capacity, Available Beds
- Link "Reset" → clear steps
- Proceed to Step 3 button

**Step 3: Lựa Chọn Thời Hạn & Chi Phí**
- Duration dropdown:
  - 6 tháng
  - 12 tháng
- Start Date (READONLY):
  - Text: "Tự động tính tại thời điểm duyệt hợp đồng"
- Deposit Amount (READONLY):
  - Auto-calc từ room type base price (= 1 tháng)
  - Formula: depositAmount = roomType.basePrice
- Monthly Price (readonly):
  - From room type
- Total Amount (readonly):
  - Formula: totalAmount = monthlyPrice × durationMonths
- Price Preview:
  - "Chi Phí Tháng: 1.500.000 VND"
  - "Thời Hạn: 6 tháng"
  - "Tổng Tiền Phòng: 9.000.000 VND"
  - "Tiền Cọc: 1.500.000 VND"
- Submit button: "Tạo Hợp Đồng"
- Back button → Step 2

**Luộc:**
1. Admin click "Tạo Hợp Đồng"
2. Redirect (or modal) → contract_registration.html
3. Step 1: Admin tìm kiếm sinh viên
   - `GET /api/v1/students?keyword=...&page=0&size=5`
   - Click student → proceed Step 2
4. Step 2: 
   - Load: `GET /api/v1/buildings`
   - If building selected: `GET /api/v1/rooms?genderAllowed=<student gender>&buildingId=X`
   - Else: `GET /api/v1/rooms?genderAllowed=<student gender>`
   - Select room (auto-filter by gender)
5. Step 3:
   - Load: `GET /api/v1/room-types` (to fetch pricing)
   - Duration dropdown select → auto-calc totals
   - Submit: `POST /api/v1/contracts { studentId, roomId, bedId, durationMonths }`
   - Toast: "Tạo hợp đồng thành công"
   - Redirect → `/admin/contracts-management.html`

**Quy Tắc Hiển Thị:**
- Building optional: có thể skip và load tất cả phòng
- Rooms preload: sau khi xác định sinh viên (và giới tính của họ)
- Gender filter: ALWAYS active (không có dropdown để chọn giới tính phòng)
- Price readonly: Admin không nhập giá, tự động từ room type base price

---

## Luồng Người Dùng (User Flows)

### Flow 1: Student Đăng Ký & Tìm Kiếm Phòng

```
START
  ↓
[/register] → Điền thông tin
  ↓
POST /api/v1/auth/register/student
  ↓
GET accessToken
  ↓
[/user/home.html] → Xem danh sách phòng
  ↓
GET /api/v1/buildings
GET /api/v1/rooms?page=0
  ↓
Student filter buildings, gender, search
  ↓
Click "Xem Chi Tiết" phòng
  ↓
[/room-detail.html?roomId=X]
  ↓
GET /api/v1/rooms/{roomId}
  ↓
Xem giường trống
  ↓
Click "Giữ Chỗ" (Step 1)
  ↓
POST /api/v1/contracts/reservations { bedId }
  ↓
Contract PENDING created, holdExpiresAt = 10 phút
  ↓
Redirect [/user/contract-application.html] (Step 2)
```

### Flow 2: Student Nộp Hồ Sơ & Chờ Duyệt

```
[/user/contract-application.html] → Step 2
  ↓
GET /api/v1/contracts/me/pending
  ↓
Student fill form:
- Duration: 6/12 tháng
- Emergency contact info
- Guardian info
- Notes
  ↓
Countdown timer: 48 giờ để nộp
  ↓
Click "Nộp Hồ Sơ"
  ↓
PUT /api/v1/contracts/{contractId}/submit
  ↓
Contract status = PENDING, submitted = true
  ↓
Redirect [/user/contracts.html]
  ↓
GET /api/v1/contracts/me/contracts
  ↓
Student thấy contract status PENDING
  ↓
Chờ admin duyệt...
```

### Flow 3: Admin Duyệt Hợp Đồng

```
[/admin/contracts-management.html]
  ↓
GET /api/v1/contracts/pending
  ↓
Xem danh sách hợp đồng chờ duyệt
  ↓
Admin click "Duyệt" (status=PENDING)
  ↓
PUT /api/v1/contracts/{contractId}/approve
  ↓
Contract status = ACTIVE
startDate recalculated = today
  ↓
Bed: isOccupied = true, student assigned
  ↓
Admin xem hợp đồng ACTIVE
  ↓
Student nhận thông báo (future)
```

### Flow 4: Student Yêu Cầu Thay Đổi Hợp Đồng

```
[/user/contracts.html] → Xem hợp đồng ACTIVE
  ↓
Click "Yêu Cầu Thay Đổi"
  ↓
Modal/form: Chọn loại thay đổi
- EXTEND_CONTRACT (Gia hạn)
- CHANGE_ROOM (Đổi phòng)
- EARLY_TERMINATION (Chấm dứt)
- OTHER (Khác)
  ↓
Fill reason + note
  ↓
POST /api/v1/contracts/{contractId}/change-requests
  ↓
ContractChangeRequest status = PENDING
  ↓
Admin xem [/admin/contracts-management.html] Tab 2
  ↓
GET /api/v1/contracts/admin/change-requests?status=PENDING
  ↓
Admin click "Duyệt" hoặc "Từ Chối"
  ↓
PUT .../change-requests/{id}/approve (or reject)
  ↓
Request status = APPROVED/REJECTED
  ↓
If EXTEND: Contract endDate updated
  ↓
SUCCESS
```

---

## Tính Năng Chính

### 1. Quản Lý Hợp Đồng (Contract Management)

**Tính Năng:**
- ✅ Admin tạo hợp đồng (3-step form)
- ✅ Student giữ chỗ (10 phút hold)
- ✅ Student nộp hồ sơ (48 giờ)
- ✅ Admin phê duyệt / từ chối
- ✅ Hợp đồng tự động chuyển ACTIVE/EXPIRED
- ✅ Hủy hợp đồng sớm (admin)
- ✅ Yêu cầu thay đổi (extend, change room, early termination)
- ✅ Duyệt/từ chối yêu cầu thay đổi

**Business Rules:**
- Duration: 6 hoặc 12 tháng
- Deposit = 1 tháng giá phòng
- Start date = approval date (not admin input)
- One open contract per student
- Hold period: 10 phút
- Submit period: 48 giờ

### 2. Quản Lý Sinh Viên (Student Management)

**Tính Năng:**
- ✅ Admin CRUD sinh viên
- ✅ Admin upload avatar
- ✅ Student update profile
- ✅ Student change password
- ✅ Student upload avatar
- ✅ Search/filter sinh viên
- ✅ Xem contract history

### 3. Quản Lý Phòng & Giường (Room & Bed Management)

**Tính Năng:**
- ✅ Admin CRUD tòa nhà
- ✅ Admin CRUD phòng
- ✅ Admin CRUD loại phòng (room type)
- ✅ Manage bed occupancy
- ✅ Visual bed layout
- ✅ Gender compatibility filter
- ✅ Room status: AVAILABLE/FULL/MAINTENANCE
- ✅ Available beds counter

### 4. Giới Tính & Tương Thích (Gender Compatibility)

**Tính Năng:**
- ✅ Building có genderAllowed
- ✅ Room thừa kế gender từ building
- ✅ Student gender match phòng gender
- ✅ Gender normalization (Nam/Nữ)
- ✅ Filter phòng theo giới tính sinh viên
- ✅ Multi-level gender check

### 5. Hiển Thị & Phân Trang (Pagination & Display)

**Tính Năng:**
- ✅ Pagination: page, size, sortBy, direction
- ✅ Default page size: 10
- ✅ Default sort: id (desc) hoặc createdAt
- ✅ Multi-column search
- ✅ Status badges (color-coded)
- ✅ Date formatting (VN format)
- ✅ Currency formatting (VND)

### 6. Lỗi & Xác Thực (Error Handling & Validation)

**Tính Năng:**
- ✅ Client-side validation (HTML5 + JS)
- ✅ Server-side validation (Jakarta Validation)
- ✅ Error messages UI (toast, modal)
- ✅ @RestControllerAdvice global exception handling
- ✅ Proper HTTP status codes
- ✅ Meaningful error messages

---

## Cấu Hình & Thiết Kế

### Color Theme
```
Primary: #0EA5A5 (Teal)
Primary Hover: #0B8F8F
Secondary: #3B82F6 (Blue)
Accent: #10B981 (Green)
Background Page: #F8FAFC
Background Card: #FFFFFF
Text Main: #0F172A
Text Muted: #475569
Border: #E2E8F0
Success: #16A34A (Green)
Warning: #D97706 (Amber)
Danger: #DC2626 (Red)
```

### Typography
```
Heading: Be Vietnam Pro (600/700)
Body: Inter (400/500)
```

### Spacing & Radius
```
Padding: 16px, 24px, 32px
Margin: 12px, 16px, 24px
Border Radius: 12px (base)
Container Max Width: 1200px
```

### Shadow
```
Soft: 0 4px 12px rgba(15, 23, 42, 0.06)
```

### Component Sizes
```
Button Height: 36-44px
Input Height: 36-40px
Card Padding: 20-24px
Gap: 12-16px
```

---

**End of Document**
