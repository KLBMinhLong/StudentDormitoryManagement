# Coding Standards - Student Dormitory Management

## Naming Convention

- Java class: PascalCase.
- Java method/field: camelCase.
- SQL Server table/column: snake_case (explicit with `@Table`, `@Column`).
- API endpoint: kebab-case (example: `/api/v1/room-types`).

## API Response Contract

- Use one response wrapper only:
  - `ApiResponse<T> { code, message, result }`
- Controllers must return `ResponseEntity<ApiResponse<T>>`.

## Global Exception Contract

- Use `@RestControllerAdvice` for all exceptions.
- Required error codes:
  - 400 Validation Error
  - 404 Not Found
  - 500 Internal Server Error

## DTO Rule (No Entity in Controller)

- Do not return Entity from controller.
- Request body type: `*RequestDTO`.
- Response body type: `*ResponseDTO`.
- Mapping via ModelMapper or MapStruct.

## UI Consistency

- Use `ui-ux-pro-max` workflow before implementing UI.
- Default stack: `html-tailwind`.
- Project visual style: simple, gentle, data-friendly.
- Mandatory color tokens:
  - Primary: #0EA5A5
  - Primary Hover: #0B8F8F
  - Secondary: #3B82F6
  - Accent: #10B981
  - Page Background: #F8FAFC
  - Card Background: #FFFFFF
  - Main Text: #0F172A
  - Muted Text: #475569
  - Border: #E2E8F0
  - Success: #16A34A
  - Warning: #D97706
  - Danger: #DC2626
- Typography tokens:
  - Heading: Be Vietnam Pro (600/700)
  - Body/UI: Inter (400/500)
- Layout tokens:
  - Container max width: 1200px
  - Radius: 12px
  - Section spacing: 24px mobile, 32px desktop
  - Card shadow: 0 4px 12px rgba(15, 23, 42, 0.06)
- Do not use purple-dominant palette in this project unless explicitly requested.
- A11y minimum: keyboard-friendly, focus-visible, semantic HTML, readable contrast.
- Text content rule: all Vietnamese UI labels/messages/placeholders must use full diacritics.
- Input data rule: user-entered Vietnamese content must preserve diacritics (do not strip accents).
