/*
Step 4 - Performance Indexes for hot API paths
Target DB: Microsoft SQL Server

Run in the application database as a user with ALTER permission.
Safe to re-run: each index is guarded by IF NOT EXISTS.
*/

SET NOCOUNT ON;

/* =========================
   INVOICE
   ========================= */
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_invoice_room_month_year_status'
      AND object_id = OBJECT_ID('[dbo].[invoice]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_invoice_room_month_year_status
    ON [dbo].[invoice] ([room_id], [month], [year], [status])
    INCLUDE ([student_id], [total_amount], [due_at], [payment_provider], [invoice_code], [created_at]);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_invoice_student_month_year_status'
      AND object_id = OBJECT_ID('[dbo].[invoice]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_invoice_student_month_year_status
    ON [dbo].[invoice] ([student_id], [month], [year], [status])
    INCLUDE ([room_id], [total_amount], [due_at], [payment_provider], [invoice_code], [created_at]);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_invoice_status_due_at'
      AND object_id = OBJECT_ID('[dbo].[invoice]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_invoice_status_due_at
    ON [dbo].[invoice] ([status], [due_at])
    INCLUDE ([overdue_marked_at], [paid_at], [room_id], [student_id]);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_invoice_payment_order_code'
      AND object_id = OBJECT_ID('[dbo].[invoice]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_invoice_payment_order_code
    ON [dbo].[invoice] ([payment_order_code]);
END;

/* =========================
   CONTRACT
   ========================= */
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_contract_student_status_created_at'
      AND object_id = OBJECT_ID('[dbo].[contract]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_contract_student_status_created_at
    ON [dbo].[contract] ([student_id], [status], [created_at] DESC)
    INCLUDE ([room_id], [bed_id], [hold_expires_at], [start_date], [end_date], [activated_at]);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_contract_room_status'
      AND object_id = OBJECT_ID('[dbo].[contract]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_contract_room_status
    ON [dbo].[contract] ([room_id], [status])
    INCLUDE ([student_id], [bed_id], [start_date], [end_date], [activated_at]);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_contract_bed_status_created_at'
      AND object_id = OBJECT_ID('[dbo].[contract]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_contract_bed_status_created_at
    ON [dbo].[contract] ([bed_id], [status], [created_at] DESC)
    INCLUDE ([student_id], [hold_expires_at], [activated_at], [end_date]);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_contract_status_hold_expires_at'
      AND object_id = OBJECT_ID('[dbo].[contract]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_contract_status_hold_expires_at
    ON [dbo].[contract] ([status], [hold_expires_at]);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_contract_status_end_date'
      AND object_id = OBJECT_ID('[dbo].[contract]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_contract_status_end_date
    ON [dbo].[contract] ([status], [end_date]);
END;

/* =========================
   CONTRACT_CHANGE_REQUEST
   ========================= */
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_ccr_student_status_created_at'
      AND object_id = OBJECT_ID('[dbo].[contract_change_request]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_ccr_student_status_created_at
    ON [dbo].[contract_change_request] ([student_id], [status], [created_at] DESC)
    INCLUDE ([contract_id], [change_type], [resolved_at]);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_ccr_contract_status_created_at'
      AND object_id = OBJECT_ID('[dbo].[contract_change_request]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_ccr_contract_status_created_at
    ON [dbo].[contract_change_request] ([contract_id], [status], [created_at] DESC)
    INCLUDE ([change_type], [resolved_at], [student_id]);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_ccr_status_change_type_created_at'
      AND object_id = OBJECT_ID('[dbo].[contract_change_request]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_ccr_status_change_type_created_at
    ON [dbo].[contract_change_request] ([status], [change_type], [created_at] DESC)
    INCLUDE ([contract_id], [student_id], [resolved_at]);
END;

/* =========================
   UTILITY_RECORD
   ========================= */
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_utility_record_room_year_month'
      AND object_id = OBJECT_ID('[dbo].[utility_record]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_utility_record_room_year_month
    ON [dbo].[utility_record] ([room_id], [year], [month])
    INCLUDE ([period_status], [old_electric], [new_electric], [old_water], [new_water], [created_at]);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_utility_record_year_month_period_status'
      AND object_id = OBJECT_ID('[dbo].[utility_record]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_utility_record_year_month_period_status
    ON [dbo].[utility_record] ([year], [month], [period_status])
    INCLUDE ([room_id], [created_at]);
END;

/* =========================
   ROOM
   ========================= */
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_room_building_status_room_number'
      AND object_id = OBJECT_ID('[dbo].[room]')
)
BEGIN
    CREATE NONCLUSTERED INDEX IX_room_building_status_room_number
    ON [dbo].[room] ([building_id], [status], [room_number]);
END;

/* Refresh statistics after index creation */
EXEC sp_updatestats;

PRINT 'Step 4 indexes applied successfully.';
