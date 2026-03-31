# Hướng Dẫn Deployment & Quick Reference

**Phiên bản:** 1.0  
**Cập nhật:** Tháng 3, 2026

---

## Mục Lục

1. [Quick Reference - Thông Tin Nhanh](#quick-reference--thông-tin-nhanh)
2. [Chạy Hệ Thống Locally](#chạy-hệ-thống-locally)
3. [Deployment Lên Server](#deployment-lên-server)
4. [Troubleshooting & Debugging](#troubleshooting--debugging)
5. [Common Tasks](#common-tasks)
6. [Monitoring & Maintenance](#monitoring--maintenance)

---

## Quick Reference - Thông Tin Nhanh

### Thông Tin Hệ Thống

| Thông Tin | Giá Trị |
|-----------|--------|
| **Backend Framework** | Spring Boot 4.0.4 |
| **Java Version** | JDK 25 |
| **Build Tool** | Maven 3.x |
| **Database** | Microsoft SQL Server |
| **Frontend** | Vanilla JS + Tailwind CSS |
| **Port (Local)** | 8080 |
| **Thời Gian Compile** | ~2-3 giây (Maven) |

### Demo Accounts

| User | Username | Password | Role |
|------|----------|----------|------|
| Admin | admin | admin123 | ROLE_ADMIN |
| Student 1 | SV2024001 | SV2024001 | ROLE_STUDENT |
| Student 2 | SV2024002 | SV2024002 | ROLE_STUDENT |

### URLs (Local)

| Page | URL |
|------|-----|
| Home | http://localhost:8080/home |
| Login | http://localhost:8080/login |
| Register | http://localhost:8080/register |
| Admin Dashboard | http://localhost:8080/admin |
| Student Home | http://localhost:8080/user/home.html |
| API Base | http://localhost:8080/api/v1 |

### Mạng Tài Liệu

| Tài Liệu | File |
|---------|------|
| API Documentation | `/docs/API_DOCUMENTATION.md` |
| Features & UI | `/docs/FEATURES_AND_UI_WORKFLOWS.md` |
| Database Schema | `/docs/DATABASE_SCHEMA.md` |
| Coding Standards | `/docs/CODING_STANDARDS.md` |
| Tailor: Custom Instructions | `.github/copilot-instructions.md` |

---

## Chạy Hệ Thống Locally

### Yêu Cầu Hệ Thống

**OS:**
- Windows 10/11, macOS, hoặc Linux

**Software:**
- Java JDK 25+ ([Download](https://www.oracle.com/java/technologies/javase/jdk25-archive-downloads.html))
- Git ([Download](https://git-scm.com))
- Microsoft SQL Server 2019+ hoặc SQL Server Express ([Download](https://www.microsoft.com/en-us/sql-server/sql-server-downloads))

**IDE (Tùy Chọn):**
- IntelliJ IDEA
- VS Code + Java Extension Pack
- Eclipse

### Bước 1: Clone Repository

```bash
git clone https://github.com/<your-repo>/student-dormitory-management.git
cd student-dormitory-management
```

### Bước 2: Tạo Database

**Bản không quy tắc lệnh SQL:**

```sql
-- Tạo database
CREATE DATABASE dormitory_management;

-- Chọn database
USE dormitory_management;

-- Tất cả bảng sẽ tạo tự động từ Hibernate (first run)
```

**Hoặc sử dụng SQL Server Management Studio:**
1. Mở SSMS
2. Right-click trên "Databases" → "New Database"
3. Tên: `dormitory_management`
4. Click OK

### Bước 3: Cấu Hình Kết Nối Database

**File:** `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=dormitory_management
spring.datasource.username=sa
spring.datasource.password=<your-sql-server-password>
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# Hibernate
spring.jpa.database-platform=org.hibernate.dialect.SQLServer2016Dialect
spring.jpa.hibernate.ddl-auto=create-drop  # First run: create, then: validate

# Logging
spring.jpa.show-sql=false
logging.level.root=INFO
logging.level.com.dormitory.management=DEBUG
```

**Lưu ý:**
- `ddl-auto=create-drop`: Tạo table lần đầu (data sẽ bị xóa khi stop app)
- Sau lần đầu, đổi thành `validate`
- Hoặc để `update` để tự động cập nhật schema

### Bước 4: Chạy Backend

**Cách 1: Sử dụng Maven Wrapper**

```bash
# Windows
mvnw.cmd clean package -DskipTests
mvnw.cmd spring-boot:run

# macOS/Linux
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

**Cách 2: Sử dụng IDE**

1. Mở project trong IDE
2. Right-click `ManagementApplication.java`
3. Select "Run as" → "Spring Boot App"
4. Chờ startup log
5. Truy cập http://localhost:8080

**Cách 3: Chạy JAR (sau build)**

```bash
# Build JAR
mvnw.cmd clean package -DskipTests

# Chạy JAR
java -jar target/management-0.0.1-SNAPSHOT.jar
```

### Bước 5: Xác Nhận Startup

**Output Expected:**

```
Tomcat started on port(s): 8080 (http)
Started ManagementApplication in X.XXX seconds with Y.ZZZ ms to first request
```

**URL Test:**

```bash
# Test API (tử API không cần xác thực)
curl http://localhost:8080/api/v1/buildings

# Test Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## Deployment Lên Server

### Deployment Target

**Options:**
- ☑️ Tomcat Server (TomEE, Wildfly, etc)
- ☑️ Docker Container
- ☑️ AWS, Azure, GCP
- ☑️ VPS (Linux)

### Cách 1: Deployment Lên Tomcat

**Bước 1: Build WAR**

```bash
# Chỉnh file pom.xml
# <packaging>jar</packaging> → <packaging>war</packaging>

mvnw.cmd clean package -DskipTests
```

**Bước 2: Deploy WAR**

```bash
# Copy file war tới Tomcat
cp target/management-0.0.1-SNAPSHOT.war $TOMCAT_HOME/webapps/

# Restart Tomcat
$TOMCAT_HOME/bin/catalina.sh restart
```

**Bước 3: Truy Cập**

```
http://<server-ip>:8080/management/
```

### Cách 2: Deployment Với Docker

**Dockerfile:**

```dockerfile
FROM openjdk:25-slim

WORKDIR /app

COPY target/management-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build & Run:**

```bash
# Build image
docker build -t dormitory-management:v1.0 .

# Run container
docker run -d \
  --name dorm-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:sqlserver://db-host:1433;databaseName=dormitory_management \
  -e SPRING_DATASOURCE_USERNAME=sa \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  dormitory-management:v1.0
```

### Cách 3: Deployment Lên Linux VPS

**Bước 1: Setup Server**

```bash
# SSH into server
ssh user@your-vps-ip

# Install Java
sudo apt update
sudo apt install openjdk-25-jdk

# Create app directory
mkdir -p /opt/dormitory-management
cd /opt/dormitory-management
```

**Bước 2: Upload JAR**

```bash
# From local machine
scp target/management-0.0.1-SNAPSHOT.jar user@your-vps-ip:/opt/dormitory-management/

# Or use Git
git clone https://github.com/...
cd student-dormitory-management
mvnw clean package -DskipTests
```

**Bước 3: Tạo Systemd Service**

```bash
sudo nano /etc/systemd/system/dormitory.service
```

```ini
[Unit]
Description=Dormitory Management System
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/dormitory-management
ExecStart=/usr/bin/java -jar management-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="SPRING_DATASOURCE_URL=jdbc:sqlserver://localhost:1433;databaseName=dormitory_management"
Environment="SPRING_DATASOURCE_USERNAME=sa"
Environment="SPRING_DATASOURCE_PASSWORD=<password>"

[Install]
WantedBy=multi-user.target
```

**Bước 4: Chạy Service**

```bash
sudo systemctl daemon-reload
sudo systemctl enable dormitory.service
sudo systemctl start dormitory.service
sudo systemctl status dormitory.service
```

---

## Troubleshooting & Debugging

### Problem 1: Port 8080 Đã Sử Dụng

**Error:**
```
Address already in use: :::8080
```

**Solution:**

```bash
# Windows: Find process on port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# macOS/Linux
lsof -i :8080
kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

---

### Problem 2: Database Connection Failed

**Error:**
```
The TCP/IP connection to the host has failed. Connection refused
```

**Solution:**

1. Kiểm tra SQL Server đang chạy:
   ```bash
   # Windows
   services.msc (tìm "SQL Server" service)
   
   # macOS/Linux
   systemctl status mssql-server
   ```

2. Kiểm tra connection string:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=dormitory_management
   spring.datasource.username=sa
   spring.datasource.password=<password>
   ```

3. Firewall: Mở port 1433
   ```bash
   # Windows Firewall
   netsh advfirewall firewall add rule name="SQL Server" dir=in action=allow protocol=tcp localport=1433
   ```

---

### Problem 3: Javax Validation Exception

**Error:**
```
org.springframework.beans.factory.UnsatisfiedDependencyException: 
Error creating bean with name 'modelMapperConfig'
```

**Solution:**

Add to `pom.xml`:

```xml
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
</dependency>
```

---

### Problem 4: CORS Error (Frontend)

**Error:**
```
Access to fetch at 'http://localhost:8080/api/v1/...' has been blocked by CORS policy
```

**Solution:**

CORS đã configured trong `SecurityConfig.java`. Kiểm tra:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        // ...
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        // ...
    }
}
```

---

### Problem 5: JWT Token Invalid

**Error:**
```
"code":401,"message":"Unauthorized"
```

**Solution:**

1. Token đã hết hạn: Đăng nhập lại
2. Token không hợp lệ: Xóa localStorage & refresh
   ```javascript
   localStorage.removeItem('accessToken');
   location.reload();
   ```
3. Bearer prefix: Kiểm tra header
   ```javascript
   fetch(url, {
     headers: {
       'Authorization': 'Bearer ' + token  // ← Bearer prefix
     }
   });
   ```

---

### Problem 6: 500 Internal Server Error

**Error:**
```
"code":500,"message":"Internal Server Error"
```

**Solution:**

1. Kiểm tra console (backend logs)
2. Kiểm tra database connectivity
3. Trace full stack trace:
   ```java
   // In GlobalExceptionHandler.java
   e.printStackTrace();  // hoặc logger
   ```
4. Tìm specific error message trong logs

---

### Problem 7: Hot Reload Không Hoạt Động

**Error:**
```
Changes không apply khi save file
```

**Solution:**

1. Add DevTools:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-devtools</artifactId>
       <scope>runtime</scope>
       <optional>true</optional>
   </dependency>
   ```

2. Enable IDE auto-compile:
   - IntelliJ: Settings → Build → Compiler → "Build project automatically"
   - VS Code: Auto Save enabled

3. Restart application:
   ```bash
   mvnw spring-boot:run
   ```

---

## Common Tasks

### Task 1: Thêm Dữ Liệu Test

**Sửa `DataInitializer.java`:**

```java
@Component
@RequiredArgsConstructor
public class DataInitializer {
    private final StudentRepository studentRepository;
    
    @PostConstruct
    public void initData() {
        // Thêm sinh viên test
        Student student = Student.builder()
            .studentCode("SV2024001")
            .fullName("Nguyễn Văn A")
            .email("nguyenvana@uni.edu.vn")
            .phone("0912345678")
            .gender("Nam")
            .cccd("123456789")
            .build();
        
        studentRepository.save(student);
    }
}
```

Restart app để chạy DataInitializer

---

### Task 2: Tạo New API Endpoint

**Bước 1: Tạo Controller Method**

```java
@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {
    
    @PostMapping("/example")
    public ResponseEntity<ApiResponse<String>> example(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(
            ApiResponse.success(ErrorCode.SUCCESS.getCode(), "Success", "Hello World")
        );
    }
}
```

**Bước 2: Tạo Service (nếu cần)**

```java
// DemoService.java
@Service
public class DemoService {
    public String doSomething() {
        return "Result";
    }
}
```

**Bước 3: Test API**

```bash
curl -X POST http://localhost:8080/api/v1/demo/example \
  -H "Content-Type: application/json" \
  -d '{"test":"value"}'
```

---

### Task 3: Change Database

**Connection String SQL Server:**

```properties
# Local default
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=dormitory_management

# Remote server
spring.datasource.url=jdbc:sqlserver://192.168.1.100:1433;databaseName=dormitory_management;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;

# Azure SQL Database
spring.datasource.url=jdbc:sqlserver://server-name.database.windows.net:1433;database=dormitory_management;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
```

---

### Task 4: Enable Debug Logging

**File: `src/main/resources/application-dev.properties`**

```properties
logging.level.root=INFO
logging.level.com.dormitory.management=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.security=DEBUG

# Log SQL parameters
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

**Chạy với profile:**

```bash
mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

---

### Task 5: Backup Database

```bash
# SQL Server Backup (Windows)
sqlcmd -S localhost -U sa -P <password> -Q "BACKUP DATABASE [dormitory_management] TO DISK = 'C:\backup\dormitory_management.bak'"

# Restore
sqlcmd -S localhost -U sa -P <password> -Q "RESTORE DATABASE [dormitory_management] FROM DISK = 'C:\backup\dormitory_management.bak'"
```

---

## Monitoring & Maintenance

### Health Check Endpoint

```bash
# Check if app is running
curl http://localhost:8080/api/v1/auth/me

# Should return 401 if no token (expected)
# Should return 200 if has valid token
```

### View Logs

**Local (Maven):**
```bash
# View realtime logs
mvnw spring-boot:run -Dspring-boot.run.arguments="--logging.level.root=INFO"

# View file logs (tạo logging.file.name trong properties)
tail -f logs/application.log
```

**Production (Linux):**
```bash
sudo journalctl -u dormitory.service -f
```

---

### Performance Monitoring

**Add to pom.xml (optional):**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Access metrics:**

```bash
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/health
```

---

### Database Maintenance

**Reindex tables:**

```sql
DBCC REINDEX
# Hoặc:
ALTER INDEX ALL ON contract REBUILD;
```

**Update statistics:**

```sql
EXEC sp_updatestats;
```

**Check database size:**

```sql
SELECT 
    db_name(database_id) as DatabaseName,
    SUM(size) * 8 / 1024 / 1024 as SizeInGB
FROM sys.master_files
WHERE database_id = db_id('dormitory_management')
GROUP BY database_id;
```

---

### Branch & Version Control

```bash
# Main branches
main          - Production ready (stable)
develop       - Development branch (unstable)
feature/*     - Feature branches
hotfix/*      - Hotfix branches

# Common workflow
git checkout develop
git pull
git checkout -b feature/new-feature
# Make changes
git add .
git commit -m "feat: add new feature"
git push origin feature/new-feature
# Create Pull Request on GitHub
```

---

### Migration Changelog

**Keep track of schema changes:**

```
v1.0 - Initial release
├── 16 tables created
├── Basic CRUD for all entities
└── Contract management workflow

v1.1 - Enhanced contract workflow
├── Added contract change requests
├── Gender compatibility filtering
└── Approval-based date calculation
```

---

**End of Deployment Guide**
