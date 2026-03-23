# Copilot Instructions - Dormitory Management

These rules are mandatory for AI-generated code in this repository.

## 1) Naming Convention

- Java class names: PascalCase (e.g., `BuildingController`, `RoomTypeService`).
- Java methods and variables: camelCase (e.g., `getBuildingById`, `totalFloors`).
- Database table and column names: snake_case via JPA annotations.
- API endpoints: kebab-case nouns (e.g., `/api/v1/room-types`, `/api/v1/utility-records`).

## 2) Standard API Response

- Always use `ApiResponse<T>` with 3 fields only: `code`, `message`, `result`.
- Every controller method must return `ResponseEntity<ApiResponse<T>>`.
- Success examples:
  - GET/PUT: `code=200`
  - POST: `code=201`
  - DELETE: `code=204`, `result=null`

## 3) Exception Handling

- Use global handling with `@RestControllerAdvice` only.
- Map and standardize error codes:
  - 400: validation/business input error
  - 404: not found
  - 500: internal server error
- Error response body must also follow `ApiResponse<T>` format.

## 4) DTO-first Rule (No Entity in Controller)

- Never expose JPA entities directly from controllers.
- Input uses `*RequestDTO`; output uses `*ResponseDTO`.
- Mapping is required via ModelMapper or MapStruct.
- Service layer handles mapping and business logic; controller orchestrates request/response only.

## 5) UI Consistency Rule

For all web UI tasks in this project:

- Use stack: `html-tailwind` by default unless user requests another stack.
- Run design-system workflow first with `ui-ux-pro-max` search script.
- Use this fixed design token set (do not pick random palettes):
  - `color-primary`: #0EA5A5
  - `color-primary-hover`: #0B8F8F
  - `color-secondary`: #3B82F6
  - `color-accent`: #10B981
  - `color-bg-page`: #F8FAFC
  - `color-bg-card`: #FFFFFF
  - `color-text-main`: #0F172A
  - `color-text-muted`: #475569
  - `color-border`: #E2E8F0
  - `color-success`: #16A34A
  - `color-warning`: #D97706
  - `color-danger`: #DC2626
- Typography must be:
  - Heading: Be Vietnam Pro (600/700)
  - Body: Inter (400/500)
- Spacing and shape baseline:
  - Container max width: 1200px
  - Section spacing: 24px mobile, 32px desktop
  - Radius: 12px for card/input/button
  - Shadow: soft only (`0 4px 12px rgba(15, 23, 42, 0.06)`)
- Keep visual style simple and gentle:
  - avoid noisy gradients, oversaturated colors, and heavy effects
  - avoid purple-dominant palettes for this project
- Keep components consistent across pages (button, input, card, table, badge).
- Enforce accessibility: semantic HTML, visible focus, keyboard navigation, sufficient color contrast.
