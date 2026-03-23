# Student Dormitory Management System

A comprehensive web application for managing student dormitory operations, including building management, room allocation, student records, contracts, utilities, invoices, and maintenance issues.

## 🏗️ Technology Stack

### Backend
- **Language:** Java 25
- **Framework:** Spring Boot 4.0.4
- **Database:** Microsoft SQL Server
- **ORM:** Spring Data JPA (Hibernate)
- **Build Tool:** Maven 3.x
- **Code Generation:** Lombok
- **Validation:** Jakarta Validation API
- **Security:** Spring Security
- **Mapping:** ModelMapper 3.2.1

### Frontend
- **Markup:** HTML5
- **Styling:** Tailwind CSS (via CSS variables)
- **Scripting:** Vanilla JavaScript (ES6+)
- **Fonts:** Be Vietnam Pro (heading), Inter (body)
- **Icon Library:** Heroicons / Lucide (recommended)

### DevOps & Tools
- **Container:** Docker (optional)
- **Version Control:** Git
- **Package Manager:** npm / yarn (for frontend assets if needed)

---

## 📋 Project Structure

```
student-dormitory-management/
├── .github/
│   ├── copilot-instructions.md          # AI coding rules
│   └── prompts/
│       └── ui-ux-pro-max/               # UI design workflow
├── docs/
│   ├── CODING_STANDARDS.md              # Developer guidelines
│   ├── TAILWIND_THEME_SNIPPET.md        # Tailwind config example
│   └── UI_QUICK_START.md                # Frontend setup guide
├── src/
│   ├── main/
│   │   ├── java/com/dormitory/management/
│   │   │   ├── config/                  # Spring configs (Security, ModelMapper, Swagger)
│   │   │   ├── controller/              # REST endpoints
│   │   │   ├── service/                 # Business logic interfaces
│   │   │   ├── service/impl/            # Service implementations
│   │   │   ├── repository/              # Spring Data JPA repositories
│   │   │   ├── entity/                  # JPA Entity classes + enums
│   │   │   ├── dto/
│   │   │   │   ├── common/              # ApiResponse<T> wrapper
│   │   │   │   ├── building/            # Building DTOs
│   │   │   │   └── error/               # Error response DTOs
│   │   │   └── exception/               # Custom exceptions + global handler
│   │   └── resources/
│   │       ├── application.properties   # App configuration
│   │       └── static/
│   │           ├── index.html           # Main dashboard page
│   │           └── ui/
│   │               └── theme.css        # Project design tokens
│   └── test/                            # Unit & integration tests
├── pom.xml                              # Maven configuration
└── README.md                            # This file

```

---

## 🎯 Core Features

### 1. Building Management
- **Entities:** Building (name, totalFloors, description)
- **Operations:** CRUD via `/api/v1/buildings`
- **Data:** Auto-synced to SQL Server, with timestamps (createdAt, updatedAt)

### 2. Room Management
- **Entities:** Room (roomNumber, status, building_id, roomType_id)
- **Status:** AVAILABLE | FULL | MAINTENANCE
- **Relations:** Each room belongs to 1 building and 1 room type

### 3. Room Type Management
- **Entities:** RoomType (name, capacity, basePrice, genderAllowed)
- **Purpose:** Classify room tiers and pricing

### 4. Bed Management
- **Entities:** Bed (bedNumber, isOccupied, room_id, student_id)
- **Relations:** Each bed is in 1 room, can host 1 student (nullable)

### 5. Student Management
- **Entities:** Student (studentCode, fullName, dateOfBirth, gender, phone, cccd, email)
- **Unique Constraints:** studentCode, cccd
- **Relations:** 1 student can sign contracts and create issues

### 6. Contract Management
- **Entities:** Contract (startDate, endDate, depositAmount, status, student_id, room_id, bed_id)
- **Status:** ACTIVE | EXPIRED | CANCELLED
- **Purpose:** Record dormitory rental agreements

### 7. Utility Records
- **Entities:** UtilityRecord (month, year, oldElectric, newElectric, oldWater, newWater, room_id)
- **Purpose:** Track monthly electric & water consumption per room

### 8. Invoice Management
- **Entities:** Invoice (month, year, roomFee, electricFee, waterFee, totalAmount, status, room_id)
- **Status:** UNPAID | PAID | OVERDUE
- **Purpose:** Generate and track monthly billing

### 9. Maintenance Issues
- **Entities:** Issue (description, priority, status, student_id, room_id)
- **Priority:** LOW | MEDIUM | HIGH
- **Status:** PENDING | IN_PROGRESS | RESOLVED
- **Purpose:** Report and resolve dormitory problems

---

## 🎨 Design System

### Colors (Fixed Palette)
| Role | Hex | Purpose |
|------|-----|---------|
| Primary | #0EA5A5 | Main interactive elements |
| Primary Hover | #0B8F8F | Hover state for primary |
| Secondary | #3B82F6 | Secondary buttons, accents |
| Accent | #10B981 | Success, positive actions |
| Page Background | #F8FAFC | Main page background |
| Card Background | #FFFFFF | Card/modal surfaces |
| Main Text | #0F172A | Primary text color |
| Muted Text | #475569 | Secondary text, labels |
| Border | #E2E8F0 | Lines, dividers, borders |
| Success | #16A34A | Success badges, states |
| Warning | #D97706 | Warning badges, alerts |
| Danger | #DC2626 | Error badges, destructive actions |

### Typography
- **Heading:** Be Vietnam Pro (weights: 600, 700)
- **Body:** Inter (weights: 400, 500)
- **Font Stack:** Web-safe fallbacks to sans-serif

### Spacing & Shape
- **Container Max Width:** 1200px
- **Border Radius:** 12px (cards, buttons, inputs)
- **Shadow:** `0 4px 12px rgba(15, 23, 42, 0.06)` (soft only)
- **Section Spacing:** 24px (mobile), 32px (desktop)

### Rules
- No purple-dominant palettes in this project
- All colors defined as CSS variables in `/static/ui/theme.css`
- Tailwind config snippet available in `docs/TAILWIND_THEME_SNIPPET.md`

---

## 🚀 Quick Start

### Prerequisites
- Java 25+
- Maven 3.6+
- SQL Server 2019+ (or SQL Server 2022)
- Node.js (optional, for frontend build tools)

### Database Setup

1. **Create Database:**
   ```sql
   CREATE DATABASE StudentDormitoryManagement;
   USE StudentDormitoryManagement;
   ```

2. **Configure Connection (application.properties):**
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=StudentDormitoryManagement;encrypt=true;trustServerCertificate=true
   spring.datasource.username=sa
   spring.datasource.password=YourStrong@Passw0rd
   ```

3. **Hibernate Auto-Create Tables:**
   - Set `spring.jpa.hibernate.ddl-auto=update` in `application.properties`
   - Run the app once; tables will auto-generate

### Backend Setup

1. **Clone Repository:**
   ```bash
   git clone <repository-url>
   cd StudentDormitoryManagement
   ```

2. **Install Dependencies:**
   ```bash
   ./mvnw.cmd clean install
   ```

3. **Run Application:**
   ```bash
   ./mvnw.cmd spring-boot:run
   ```

4. **Access Application:**
   - Main Dashboard: `http://localhost:8080/`
   - Building API: `http://localhost:8080/api/v1/buildings`

### Frontend Setup

Include the theme in your HTML:
```html
<link rel="stylesheet" href="/ui/theme.css">
<link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@600;700&family=Inter:wght@400;500&display=swap" rel="stylesheet">
```

Or use Tailwind with the provided config snippet:
See `docs/TAILWIND_THEME_SNIPPET.md` for detailed Tailwind `config.js` example.

---

## 📡 API Specification

### Standard Response Format

All API responses follow the `ApiResponse<T>` wrapper:

```json
{
  "code": 200,
  "message": "Success message",
  "result": { ... }
}
```

### Error Codes
- **200:** Success (GET, PUT)
- **201:** Created (POST)
- **204:** No Content (DELETE)
- **400:** Bad Request (validation, business logic errors)
- **404:** Not Found (resource doesn't exist)
- **500:** Internal Server Error

### Building Endpoints

#### Get All Buildings
```
GET /api/v1/buildings
Response: { code: 200, message: "...", result: [BuildingDTO, ...] }
```

#### Get Building by ID
```
GET /api/v1/buildings/{id}
Response: { code: 200, message: "...", result: BuildingDTO }
```

#### Create Building
```
POST /api/v1/buildings
Body: { name: "string", description: "string" }
Response: { code: 201, message: "...", result: BuildingDTO }
```

#### Update Building
```
PUT /api/v1/buildings/{id}
Body: { name: "string", description: "string" }
Response: { code: 200, message: "...", result: BuildingDTO }
```

#### Delete Building
```
DELETE /api/v1/buildings/{id}
Response: { code: 204, message: "...", result: null }
```

---

## 🔐 Coding Standards

All code must adhere to the rules in [.github/copilot-instructions.md](.github/copilot-instructions.md) and [docs/CODING_STANDARDS.md](docs/CODING_STANDARDS.md).

### Key Rules

**1. Naming Convention**
- Java classes: PascalCase (`BuildingController`, `RoomService`)
- Java methods/variables: camelCase (`getBuildingById`, `totalFloors`)
- Database tables/columns: snake_case (`building`, `room_number`)
- API endpoints: kebab-case (`/api/v1/room-types`)

**2. DTO-First Rule**
- Never return Entity from controller
- Request body: `*RequestDTO`
- Response body: `*ResponseDTO`
- Mapping via ModelMapper

**3. Exception Handling**
- Use `@RestControllerAdvice` for global handling
- All errors return `ApiResponse` format
- Map error codes: 400, 404, 500

**4. Lombok Usage**
- All entities use `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- Exclude circular references in `@EqualsAndHashCode.Exclude`, `@ToString.Exclude`
- Add `@JsonIgnore` to collection properties to prevent recursion

**5. UI Development**
- Default stack: `html-tailwind`
- Run `ui-ux-pro-max` workflow before implementing UI
- Use fixed color palette from design system
- Enforce accessibility (a11y): semantic HTML, keyboard navigation, color contrast

---

## 📂 Frontend Pages

### Dashboard (None)
- **URL:** `http://localhost:8081/`
---

## 🧪 Testing

### Unit Tests
Located in `src/test/java/...`

```bash
./mvnw.cmd test
```

### Integration Tests
- Test core service logic
- Mock repositories or use in-memory H2 for quick tests

### Manual API Testing
Use Postman or cURL to test endpoints:

```bash
# Get all buildings
curl -X GET http://localhost:8080/api/v1/buildings

# Create building
curl -X POST http://localhost:8080/api/v1/buildings \
  -H "Content-Type: application/json" \
  -d '{"name":"Building A","description":"Main campus"}'
```

---

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| [.github/copilot-instructions.md](.github/copilot-instructions.md) | AI code generation rules |
| [docs/CODING_STANDARDS.md](docs/CODING_STANDARDS.md) | Developer coding guidelines |
| [docs/TAILWIND_THEME_SNIPPET.md](docs/TAILWIND_THEME_SNIPPET.md) | Tailwind CSS configuration |
| [docs/UI_QUICK_START.md](docs/UI_QUICK_START.md) | Frontend integration guide |
| [README.md](README.md) | This file |

---

## 🔄 Development Workflow

### Add a New Feature (e.g., New Entity)

1. **Create Entity** in `entity/` directory
   - Extend `BaseEntity`
   - Use Lombok annotations
   - Define relationships with `@OneToMany`, `@ManyToOne`, etc.

2. **Create DTOs** in `dto/`
   - `*RequestDTO` for input validation
   - `*ResponseDTO` for output

3. **Create Repository** in `repository/`
   - Extend `JpaRepository<Entity, Long>`
   - Add custom query methods if needed

4. **Create Service Interface** in `service/`
   - Define business logic methods
   - Return DTOs, not entities

5. **Create Service Implementation** in `service/impl/`
   - Implement interface
   - Use ModelMapper for DTO conversion
   - Handle business logic and validation

6. **Create Controller** in `controller/`
   - Return `ResponseEntity<ApiResponse<T>>`
   - Map endpoints to `/api/v1/` path with kebab-case names
   - Validate input with `@Valid`

7. **Test All Endpoints** via Postman or cURL

### UI Changes

1. Run design-system search if changing layout/colors:
   ```bash
   python3 .github/prompts/ui-ux-pro-max/scripts/search.py "your keywords" --design-system
   ```

2. Override colors if needed, but prefer the fixed palette
3. Use `/ui/theme.css` variables or Tailwind classes
4. Test responsive at 375px, 768px, 1024px, 1440px viewports

---

## 🐛 Troubleshooting

### Database Connection Issues
- Verify SQL Server is running
- Check credentials in `application.properties`
- Ensure database `StudentDormitoryManagement` exists

### Hibernate DDL Errors
- If tables don't auto-create, manually run SQL scripts
- Check `spring.jpa.hibernate.ddl-auto=update` setting

### API Returns 404
- Verify endpoint URL matches controller `@RequestMapping` path
- Check if Spring Security permits the route (currently all routes are open in dev)

### Frontend Not Loading
- Verify static resources are in `src/main/resources/static/`
- Check browser console for 404 errors on CSS/JS files
- Ensure fonts load from Google Fonts CDN

---

## 📞 Support & Contributing

- **Issues?** Create a GitHub issue with detailed description
- **PRs?** Follow coding standards and ensure tests pass
- **Questions?** Reference docs/ folder for guides

---

## 📄 License

This project is part of a university dormitory management assignment.
Use for educational purposes only.

---

**Last Updated:** March 23, 2026  
**Maintainer:** Student Dormitory Management Team  
**Java Version:** 25  
**Spring Boot Version:** 4.0.4
