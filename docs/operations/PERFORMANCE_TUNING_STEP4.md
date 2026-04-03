# Step 4 - Apply DB Indexes for Slow APIs

## Goal
Giảm thời gian phản hồi cho các API danh sách/hot path sau khi đã tối ưu code ở Step 1-3.

## Files
- SQL script: [PERFORMANCE_INDEXES_STEP4.sql](PERFORMANCE_INDEXES_STEP4.sql)

## How to run
1. Backup database hoặc chạy trước trên staging.
2. Mở SQL Server Management Studio.
3. Chọn đúng database của hệ thống.
4. Chạy toàn bộ script `PERFORMANCE_INDEXES_STEP4.sql`.

## Verify indexes created
```sql
SELECT t.name AS table_name, i.name AS index_name
FROM sys.indexes i
JOIN sys.tables t ON i.object_id = t.object_id
WHERE i.name LIKE 'IX_%'
  AND t.name IN ('invoice', 'contract', 'contract_change_request', 'utility_record', 'room')
ORDER BY t.name, i.name;
```

## Re-measure after deployment
Đo lại p95/p99 cho các API sau:
- `GET /api/v1/invoices/admin/list`
- `GET /api/v1/contracts/admin/management`
- `GET /api/v1/contracts/admin/change-requests`
- `GET /api/v1/utility-records/history`
- `GET /api/v1/invoices/admin/utility-records`

## Notes
- Script có `IF NOT EXISTS`, có thể chạy lại an toàn.
- Nếu dữ liệu lớn, nên chạy ngoài giờ cao điểm để tránh lock lâu.
- Với SQL Server edition hỗ trợ, có thể cân nhắc ONLINE index operations cho môi trường production lớn.
