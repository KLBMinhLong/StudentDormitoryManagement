# Hướng Dẫn Chạy Dự Án

## 1) Yêu cầu môi trường

- JDK 21.
- Maven Wrapper (`mvnw`, `mvnw.cmd`).
- PostgreSQL.

## 2) Chạy local bằng profile dev

Bước 1: tạo database PostgreSQL, ví dụ `StudentDormitoryManagement`.

Bước 2: cấu hình biến môi trường hoặc sửa profile phù hợp:

- `SPRING_PROFILES_ACTIVE=dev`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Bước 3: chạy ứng dụng:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Mặc định ứng dụng chạy cổng `8080` (có thể override bằng `PORT`).

## 3) Build artifact

```bash
./mvnw clean package
```

File kết quả: `target/management-0.0.1-SNAPSHOT.jar`.

## 4) Chạy artifact production

```bash
java -jar target/management-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Lưu ý:

- `application-production.properties` import `application-prod.properties`.
- Nên truyền đầy đủ biến bí mật qua môi trường thay vì hardcode file.

## 5) Luồng schema dữ liệu

- `spring.jpa.hibernate.ddl-auto=none`.
- Khuyến nghị áp dụng schema bằng script migration có kiểm soát trước khi chạy.
- Khi cần init schema bằng `schema.sql`, bật cấu hình SQL init phù hợp theo môi trường.

## 6) Kiểm tra sau khi chạy

- Truy cập trang: `/login`, `/home`, `/admin`.
- Kiểm tra API auth: `/api/v1/auth/login`.
- Kiểm tra scheduler hoạt động qua log ứng dụng.

## 7) Lưu ý bảo mật triển khai

- Không dùng thông tin bí mật trong profile dev cho môi trường thật.
- Luôn rotate các secret JWT, mail, PayOS khi chuyển môi trường.
- Chỉ mở public đúng endpoint cần thiết (đặc biệt webhook).