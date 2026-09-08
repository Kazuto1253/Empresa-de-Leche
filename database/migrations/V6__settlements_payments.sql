CREATE TABLE weekly_collection_closure (
    id CHAR(36) PRIMARY KEY,
    week_start DATE NOT NULL,
    week_end DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CLOSED',
    closed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    closed_by CHAR(36) NOT NULL,
    total_liters DECIMAL(14,3) NOT NULL,
    provider_count INT NOT NULL,
    metadata JSON NULL,
    CONSTRAINT fk_weekly_closure_user FOREIGN KEY (closed_by) REFERENCES app_user(id),
    CONSTRAINT chk_weekly_closure_dates CHECK (week_end >= week_start),
    UNIQUE KEY uq_weekly_closure_period (week_start, week_end),
    INDEX idx_weekly_closure_status (status, week_start)
);

CREATE TABLE weekly_collection_closure_provider (
    closure_id CHAR(36) NOT NULL,
    provider_id CHAR(36) NOT NULL,
    field_liters DECIMAL(14,3) NOT NULL,
    direct_liters DECIMAL(14,3) NOT NULL,
    total_liters DECIMAL(14,3) NOT NULL,
    PRIMARY KEY (closure_id, provider_id),
    CONSTRAINT fk_closure_provider_closure FOREIGN KEY (closure_id) REFERENCES weekly_collection_closure(id),
    CONSTRAINT fk_closure_provider_provider FOREIGN KEY (provider_id) REFERENCES provider(id),
    INDEX idx_closure_provider (provider_id, closure_id)
);

CREATE TABLE milk_price (
    id CHAR(36) PRIMARY KEY,
    criterion VARCHAR(60) NOT NULL DEFAULT 'BASE',
    amount DECIMAL(19,6) NOT NULL,
    currency CHAR(3) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL,
    created_by CHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    reason VARCHAR(500) NOT NULL,
    CONSTRAINT fk_milk_price_user FOREIGN KEY (created_by) REFERENCES app_user(id),
    CONSTRAINT chk_milk_price_amount CHECK (amount > 0),
    CONSTRAINT chk_milk_price_dates CHECK (effective_to IS NULL OR effective_to >= effective_from),
    UNIQUE KEY uq_milk_price_version (criterion, version),
    INDEX idx_milk_price_effective (criterion, status, effective_from, effective_to)
);

CREATE TABLE settlement (
    id CHAR(36) PRIMARY KEY,
    provider_id CHAR(36) NOT NULL,
    weekly_closure_id CHAR(36) NOT NULL,
    liters DECIMAL(14,3) NOT NULL,
    base_price DECIMAL(19,6) NOT NULL,
    currency CHAR(3) NOT NULL,
    gross_amount DECIMAL(19,4) NOT NULL,
    quality_adjustment DECIMAL(19,4) NOT NULL DEFAULT 0,
    other_approved_adjustments DECIMAL(19,4) NOT NULL DEFAULT 0,
    net_amount DECIMAL(19,4) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CALCULATED',
    calculated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    calculated_by CHAR(36) NOT NULL,
    price_id CHAR(36) NOT NULL,
    price_version BIGINT NOT NULL,
    CONSTRAINT fk_settlement_provider FOREIGN KEY (provider_id) REFERENCES provider(id),
    CONSTRAINT fk_settlement_closure FOREIGN KEY (weekly_closure_id) REFERENCES weekly_collection_closure(id),
    CONSTRAINT fk_settlement_user FOREIGN KEY (calculated_by) REFERENCES app_user(id),
    CONSTRAINT fk_settlement_price FOREIGN KEY (price_id) REFERENCES milk_price(id),
    UNIQUE KEY uq_settlement_provider_week (provider_id, weekly_closure_id),
    INDEX idx_settlement_provider_status (provider_id, status, calculated_at)
);

CREATE TABLE payment (
    id CHAR(36) PRIMARY KEY,
    settlement_id CHAR(36) NOT NULL,
    provider_id CHAR(36) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency CHAR(3) NOT NULL,
    paid_at TIMESTAMP(6) NOT NULL,
    method VARCHAR(40) NOT NULL,
    reference VARCHAR(160) NULL,
    registered_by CHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_payment_settlement FOREIGN KEY (settlement_id) REFERENCES settlement(id),
    CONSTRAINT fk_payment_provider FOREIGN KEY (provider_id) REFERENCES provider(id),
    CONSTRAINT fk_payment_user FOREIGN KEY (registered_by) REFERENCES app_user(id),
    CONSTRAINT chk_payment_amount CHECK (amount > 0),
    UNIQUE KEY uq_payment_settlement (settlement_id),
    INDEX idx_payment_provider_date (provider_id, paid_at, status)
);
