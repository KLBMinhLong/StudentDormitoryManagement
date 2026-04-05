# Danh Mục API (Theo Controller Hiện Tại)

Base URL: `/api/v1`

## 1) Auth (`/auth`)

- `POST /auth/register/student`
- `POST /auth/login`
- `GET /auth/me`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`

## 2) Buildings (`/buildings`)

- `GET /buildings`
- `GET /buildings/{id}`
- `POST /buildings`
- `PUT /buildings/{id}`
- `DELETE /buildings/{id}`

## 3) Rooms and Beds (`/rooms`)

- `GET /rooms`
- `GET /rooms/{id}`
- `POST /rooms`
- `PUT /rooms/{id}`
- `DELETE /rooms/{id}`
- `GET /rooms/{roomId}/beds`
- `PUT /rooms/{roomId}/beds/{bedId}/occupancy`
- `PUT /rooms/{roomId}/beds/layout`

## 4) Room Types (`/room-types`)

- `GET /room-types`

## 5) Students (`/students`)

Admin:

- `GET /students`
- `GET /students/residents`
- `GET /students/{id}`
- `GET /students/{id}/residence-history`
- `POST /students`
- `PUT /students/{id}`
- `DELETE /students/{id}`
- `POST /students/{id}/avatar`

Student self-service:

- `GET /students/me`
- `GET /students/me/residence-history`
- `PUT /students/me`
- `PUT /students/me/password`
- `POST /students/me/avatar`

## 6) Contracts (`/contracts`)

Student:

- `POST /contracts/reservations`
- `PUT /contracts/{contractId}/submit`
- `GET /contracts/me/pending`
- `GET /contracts/me/contracts`
- `GET /contracts/me/contracts/{contractId}`
- `POST /contracts/{contractId}/change-requests`
- `GET /contracts/me/change-requests`

Admin:

- `POST /contracts`
- `PUT /contracts/{contractId}/approve`
- `PUT /contracts/{contractId}/reject`
- `GET /contracts/pending`
- `GET /contracts/admin/management`
- `GET /contracts/admin/{contractId}`
- `PUT /contracts/{contractId}/cancel-early`
- `GET /contracts/admin/change-requests`
- `PUT /contracts/change-requests/{requestId}/approve`
- `PUT /contracts/change-requests/{requestId}/reject`

## 7) Utility Records (`/utility-records`)

- `POST /utility-records`
- `POST /utility-records/batch`
- `GET /utility-records/{id}`
- `PUT /utility-records/{id}`
- `DELETE /utility-records/{id}`
- `GET /utility-records/history`
- `GET /utility-records/prefill`
- `GET /utility-records/timeline`
- `GET /utility-records/me/timeline`
- `POST /utility-records/period/close`
- `POST /utility-records/period/reopen`

## 8) Invoices (`/invoices`)

Admin:

- `GET /invoices/admin/utility-records`
- `POST /invoices/generate-monthly`
- `POST /invoices/admin/generate-room/{roomId}`
- `POST /invoices/admin/mark-overdue`
- `GET /invoices/admin/list`
- `POST /invoices/{invoiceId}/manual-approve`

Student:

- `GET /invoices/me`
- `POST /invoices/me/{invoiceId}/create-payment-link`

## 9) Pricing Policies (`/pricing-policies`)

- `GET /pricing-policies/latest`
- `POST /pricing-policies/update`

## 10) Issues (`/issues`)

Student:

- `POST /issues`
- `GET /issues/me`

Admin:

- `GET /issues`
- `GET /issues/student/{studentId}`
- `PUT /issues/{id}/status`

## 11) Admin Dashboard (`/admin/dashboard`)

- `GET /admin/dashboard/overview`

## 12) Report Export (`/reports/admin`)

- `GET /reports/admin/students/excel`
- `GET /reports/admin/contracts/excel`
- `GET /reports/admin/invoices/excel`
- `GET /reports/admin/utility-records/excel`

## 13) Payment Webhook (`/payment`)

- `POST /payment/webhook/payos`

## 14) Ghi chú

- Danh mục này phản ánh các route trong controller hiện tại.
- Chi tiết payload request/response xem DTO trong package `com.dormitory.management.dto`.
- Tất cả API JSON tuân thủ `ApiResponse<T>`.