CREATE TABLE IF NOT EXISTS building (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    name VARCHAR(150) NOT NULL UNIQUE,
    total_floors INT NOT NULL,
    gender_allowed VARCHAR(20) NOT NULL,
    description TEXT
)
@@

CREATE TABLE IF NOT EXISTS room_type (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    base_price NUMERIC(18,2) NOT NULL,
    gender_allowed VARCHAR(20) NOT NULL
)
@@

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    role_name VARCHAR(50) NOT NULL UNIQUE
)
@@

CREATE TABLE IF NOT EXISTS student (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    student_code VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    phone VARCHAR(20),
    cccd VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(120),
    avatar_url VARCHAR(255)
)
@@

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) UNIQUE,
    student_id BIGINT UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES student(id)
)
@@

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
)
@@

CREATE TABLE IF NOT EXISTS room (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    building_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    gender_allowed VARCHAR(20),
    CONSTRAINT uk_room_building_room_number UNIQUE (building_id, room_number),
    CONSTRAINT fk_room_building FOREIGN KEY (building_id) REFERENCES building(id),
    CONSTRAINT fk_room_room_type FOREIGN KEY (room_type_id) REFERENCES room_type(id)
)
@@

CREATE TABLE IF NOT EXISTS bed (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    bed_number INT NOT NULL,
    is_occupied BOOLEAN NOT NULL,
    reserved_until TIMESTAMP,
    reserved_contract_id BIGINT,
    room_id BIGINT NOT NULL,
    student_id BIGINT UNIQUE,
    CONSTRAINT fk_bed_room FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    CONSTRAINT fk_bed_student FOREIGN KEY (student_id) REFERENCES student(id)
)
@@

CREATE TABLE IF NOT EXISTS contract (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    deposit_amount NUMERIC(18,2) NOT NULL,
    monthly_room_price NUMERIC(18,2) NOT NULL,
    total_room_amount NUMERIC(18,2) NOT NULL,
    duration_months INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    hold_expires_at TIMESTAMP NOT NULL,
    is_submitted BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_at TIMESTAMP,
    activated_at TIMESTAMP,
    emergency_contact_name VARCHAR(120),
    emergency_contact_phone VARCHAR(20),
    guardian_name VARCHAR(120),
    guardian_phone VARCHAR(20),
    student_note TEXT,
    student_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    CONSTRAINT fk_contract_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_contract_room FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT fk_contract_bed FOREIGN KEY (bed_id) REFERENCES bed(id)
)
@@

CREATE TABLE IF NOT EXISTS utility_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    old_electric DOUBLE PRECISION,
    new_electric DOUBLE PRECISION,
    old_water DOUBLE PRECISION,
    new_water DOUBLE PRECISION,
    period_status VARCHAR(20) NOT NULL,
    room_id BIGINT NOT NULL,
    CONSTRAINT uk_utility_record_room_period UNIQUE (room_id, month, year),
    CONSTRAINT fk_utility_record_room FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE
)
@@

CREATE TABLE IF NOT EXISTS invoice (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    invoice_code VARCHAR(60) NOT NULL UNIQUE,
    month INT NOT NULL,
    year INT NOT NULL,
    room_fee NUMERIC(18,2) NOT NULL,
    electric_fee NUMERIC(18,2) NOT NULL,
    water_fee NUMERIC(18,2) NOT NULL,
    service_fee NUMERIC(18,2) NOT NULL,
    electric_usage NUMERIC(18,2) NOT NULL,
    water_usage NUMERIC(18,2) NOT NULL,
    electric_unit_price NUMERIC(18,2) NOT NULL,
    water_unit_price NUMERIC(18,2) NOT NULL,
    students_in_room INT NOT NULL,
    utility_amount_per_student NUMERIC(18,2) NOT NULL,
    total_amount NUMERIC(18,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    issued_at TIMESTAMP,
    due_at TIMESTAMP,
    paid_at TIMESTAMP,
    overdue_marked_at TIMESTAMP,
    paid_late BOOLEAN NOT NULL DEFAULT FALSE,
    payment_provider VARCHAR(20) NOT NULL,
    payment_order_code VARCHAR(100),
    payment_link VARCHAR(500),
    payment_qr_code TEXT,
    provider_transaction_id VARCHAR(100),
    provider_raw_payload TEXT,
    manual_approved_by VARCHAR(100),
    manual_approved_at TIMESTAMP,
    manual_approval_note TEXT,
    student_id BIGINT,
    room_id BIGINT NOT NULL,
    CONSTRAINT fk_invoice_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_invoice_room FOREIGN KEY (room_id) REFERENCES room(id)
)
@@

CREATE TABLE IF NOT EXISTS issue (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    student_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    CONSTRAINT fk_issue_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_issue_room FOREIGN KEY (room_id) REFERENCES room(id)
)
@@

CREATE TABLE IF NOT EXISTS contract_change_request (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_end_date DATE,
    reason TEXT,
    admin_note TEXT,
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(120),
    contract_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    CONSTRAINT fk_change_request_contract FOREIGN KEY (contract_id) REFERENCES contract(id),
    CONSTRAINT fk_change_request_student FOREIGN KEY (student_id) REFERENCES student(id)
)
@@

CREATE TABLE IF NOT EXISTS pricing_policy (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    electric_unit_price NUMERIC(18,2) NOT NULL,
    water_unit_price NUMERIC(18,2) NOT NULL,
    service_fee NUMERIC(18,2) NOT NULL,
    effective_from VARCHAR(100) NOT NULL,
    notes TEXT
)
@@

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    app_user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (app_user_id) REFERENCES users(id) ON DELETE CASCADE
)
@@

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'building' AND column_name = 'gender_allowed' AND udt_name = 'bytea'
    ) THEN
        ALTER TABLE building
            ALTER COLUMN gender_allowed TYPE VARCHAR(20)
            USING convert_from(gender_allowed, 'UTF8');
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'room_type' AND column_name = 'gender_allowed' AND udt_name = 'bytea'
    ) THEN
        ALTER TABLE room_type
            ALTER COLUMN gender_allowed TYPE VARCHAR(20)
            USING convert_from(gender_allowed, 'UTF8');
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'room' AND column_name = 'room_number' AND udt_name = 'bytea'
    ) THEN
        ALTER TABLE room
            ALTER COLUMN room_number TYPE VARCHAR(20)
            USING convert_from(room_number, 'UTF8');
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'room' AND column_name = 'gender_allowed' AND udt_name = 'bytea'
    ) THEN
        ALTER TABLE room
            ALTER COLUMN gender_allowed TYPE VARCHAR(20)
            USING convert_from(gender_allowed, 'UTF8');
    END IF;
END $$
@@
