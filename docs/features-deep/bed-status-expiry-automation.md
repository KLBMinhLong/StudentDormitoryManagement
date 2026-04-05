# Tính Năng: Tự Động Thay Đổi Trạng Thái Giường Khi Hết Hạn Chờ

## 1) Mục tiêu

Tự động giải phóng giường đã giữ chỗ quá hạn hoặc gắn với hợp đồng đã hết hạn, để tránh khóa tài nguyên sai và đảm bảo trạng thái phòng/giường luôn đúng.

## 2) Phạm vi tự động hóa

Có 2 nhánh tự động chính:

1. Hết hạn chờ duyệt/giữ chỗ (`PENDING`) => hủy hợp đồng chờ, trả giường về trống.
2. Hợp đồng đang hiệu lực nhưng đã qua ngày kết thúc (`ACTIVE` quá hạn) => chuyển `EXPIRED`, giải phóng giường.

## 3) Luồng xử lý tổng quát

1. Scheduler chạy mỗi 60 giây.
2. Gọi xử lý hủy hợp đồng chờ quá hạn.
3. Gọi xử lý hết hạn hợp đồng active.
4. Mỗi bản ghi hợp lệ:
   - Lock giường để tránh race condition.
   - Cập nhật trạng thái hợp đồng tương ứng.
   - Cập nhật giường: `isOccupied`, `student`, `reservedUntil`, `reservedContractId`.
   - Cập nhật trạng thái phòng theo số giường trống/có người.

## 4) Vị trí code rõ ràng

### 4.1 Điểm kích hoạt scheduler

- File: `src/main/java/com/dormitory/management/service/ContractLifecycleScheduler.java`
- Class: `ContractLifecycleScheduler`
- Method: `cancelExpiredPendingContracts`
- Line chính:
  - Scheduler fixed delay 60s: dòng 19
  - Gọi hủy pending quá hạn: dòng 21
  - Gọi expire active + release beds: dòng 26

### 4.2 Hủy hợp đồng chờ quá hạn

- File: `src/main/java/com/dormitory/management/service/impl/ContractServiceImpl.java`
- Class: `ContractServiceImpl`
- Method: `cancelExpiredPendingContracts`
- Line chính:
  - Bắt đầu method: dòng 301
  - Truy vấn pending quá hạn hold: dòng 302
  - Hủy nội bộ từng hợp đồng: dòng 304

### 4.3 Hết hạn hợp đồng active và giải phóng giường

- File: `src/main/java/com/dormitory/management/service/impl/ContractServiceImpl.java`
- Class: `ContractServiceImpl`
- Method: `expireActiveContractsAndReleaseBeds`
- Line chính:
  - Bắt đầu method: dòng 311
  - Truy vấn contract active quá `end_date`: dòng 312
  - Lock giường: dòng 314-316
  - Đổi trạng thái contract thành `EXPIRED`: dòng 318-321
  - Giải phóng giường: dòng 323-328
  - Cập nhật trạng thái phòng: dòng 329

### 4.4 Hàm lõi giải phóng giường ở nhánh cancel pending

- File: `src/main/java/com/dormitory/management/service/impl/ContractServiceImpl.java`
- Method: `cancelPendingContractInternal`
- Line chính:
  - Bắt đầu method: dòng 595
  - Lock giường: dòng 596-598
  - Đổi trạng thái contract thành `CANCELLED`: dòng 600-602
  - Trả giường về trống nếu còn giữ đúng contract: dòng 604-611

### 4.5 Hàm phòng ngừa giữ chỗ “ma” khi user đặt giường

- File: `src/main/java/com/dormitory/management/service/impl/ContractServiceImpl.java`
- Method: `ensureBedCanBeReserved`
- Line chính:
  - Bắt đầu method: dòng 632
  - Nếu giường đang reserved nhưng đã quá hạn: tự clear reservation và save: dòng 645-648

## 5) Trạng thái dữ liệu bị tác động

- `contract.status`:
  - `PENDING -> CANCELLED` (quá hạn chờ)
  - `ACTIVE -> EXPIRED` (quá hạn hợp đồng)
- `bed`:
  - `isOccupied` về `false` khi release
  - `student` về `null`
  - `reservedUntil` về `null`
  - `reservedContractId` về `null`
- `room.status`:
  - cập nhật theo số giường còn trống (`AVAILABLE`/`FULL`, trừ `MAINTENANCE`).

## 6) Rủi ro và biện pháp trong code

- Rủi ro race condition: dùng `findByIdForUpdate` để lock giường trước khi cập nhật.
- Rủi ro release nhầm giường: chỉ clear khi `reservedContractId` khớp contract hiện tại ở nhánh pending.
- Rủi ro lệch room status: luôn gọi `updateRoomStatusByBeds` sau khi release.

## 7) Checklist kiểm thử

- Contract pending quá hạn phải bị cancel và giường trả về trống.
- Contract active quá hạn phải thành expired và giường được giải phóng.
- Không giải phóng nhầm giường đang thuộc sinh viên khác.
- Room status đổi đúng khi có/không còn giường trống.
- Scheduler chạy nhiều lần không gây sai trạng thái lặp.