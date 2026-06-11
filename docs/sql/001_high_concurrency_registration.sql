CREATE TABLE IF NOT EXISTS race (
    id BIGINT PRIMARY KEY,
    race_name VARCHAR(255) NOT NULL,
    race_info TEXT NULL,
    race_type INT NOT NULL,
    race_id VARCHAR(64) NOT NULL,
    participant_number INT NOT NULL,
    registration_mode INT NOT NULL DEFAULT 1,
    enroll_start DATETIME NOT NULL,
    enroll_end DATETIME NOT NULL,
    fee DECIMAL(10, 2) NOT NULL,
    location VARCHAR(255) NOT NULL,
    race_date DATETIME NOT NULL,
    race_status INT NOT NULL,
    organizer_id BIGINT NULL,
    KEY idx_race_status_date (race_status, race_date)
);

CREATE TABLE IF NOT EXISTS apply (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type INT NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    qualification_url VARCHAR(512) NULL,
    authorization_url VARCHAR(512) NULL,
    race_name VARCHAR(255) NOT NULL,
    race_info TEXT NULL,
    race_type INT NOT NULL,
    participant_number INT NOT NULL,
    registration_mode INT NOT NULL DEFAULT 1,
    fee DECIMAL(10, 2) NOT NULL,
    location VARCHAR(255) NOT NULL,
    race_date DATETIME NOT NULL,
    organizer_id BIGINT NULL,
    apply_status INT NOT NULL,
    apply_time DATETIME NOT NULL
);

ALTER TABLE race
    ADD COLUMN IF NOT EXISTS registration_mode INT NOT NULL DEFAULT 1;

ALTER TABLE apply
    ADD COLUMN IF NOT EXISTS registration_mode INT NOT NULL DEFAULT 1;

CREATE TABLE IF NOT EXISTS registration (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    race_id BIGINT NOT NULL,
    status INT NOT NULL,
    athlete_number INT NULL,
    register_time DATETIME NOT NULL,
    UNIQUE KEY uk_registration_user_race (user_id, race_id),
    KEY idx_registration_race_status (race_id, status)
);

CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    race_id BIGINT NOT NULL,
    pay_status INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    create_time DATETIME NOT NULL,
    pay_time DATETIME NULL,
    KEY idx_order_user_race_status (user_id, race_id, pay_status)
);
