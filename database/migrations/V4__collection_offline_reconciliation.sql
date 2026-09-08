CREATE TABLE milk_collection (
    id CHAR(36) PRIMARY KEY,
    provider_id CHAR(36) NOT NULL,
    route_id CHAR(36) NOT NULL,
    workday_id CHAR(36) NOT NULL,
    collector_user_id CHAR(36) NOT NULL,
    collected_at TIMESTAMP(6) NOT NULL,
    liters DECIMAL(12,3) NOT NULL,
    origin VARCHAR(30) NOT NULL,
    client_operation_id CHAR(36) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'SYNCED',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_collection_provider FOREIGN KEY (provider_id) REFERENCES provider(id),
    CONSTRAINT fk_collection_route FOREIGN KEY (route_id) REFERENCES route(id),
    CONSTRAINT fk_collection_workday FOREIGN KEY (workday_id) REFERENCES collection_workday(id),
    CONSTRAINT fk_collection_collector FOREIGN KEY (collector_user_id) REFERENCES app_user(id),
    CONSTRAINT chk_collection_liters CHECK (liters > 0),
    INDEX idx_collection_scope (provider_id, route_id, workday_id, collected_at),
    INDEX idx_collection_status (status, collected_at)
);

CREATE TABLE direct_delivery (
    id CHAR(36) PRIMARY KEY,
    provider_id CHAR(36) NOT NULL,
    delivered_at TIMESTAMP(6) NOT NULL,
    liters DECIMAL(12,3) NOT NULL,
    origin VARCHAR(30) NOT NULL DEFAULT 'DIRECT_DELIVERY',
    responsible_user_id CHAR(36) NOT NULL,
    client_operation_id CHAR(36) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_delivery_provider FOREIGN KEY (provider_id) REFERENCES provider(id),
    CONSTRAINT fk_delivery_user FOREIGN KEY (responsible_user_id) REFERENCES app_user(id),
    CONSTRAINT chk_delivery_liters CHECK (liters > 0),
    INDEX idx_delivery_provider_date (provider_id, delivered_at)
);

CREATE TABLE plant_reception (
    id CHAR(36) PRIMARY KEY,
    workday_id CHAR(36) NULL,
    route_id CHAR(36) NULL,
    collection_unit_id CHAR(36) NULL,
    direct_delivery_id CHAR(36) NULL,
    received_at TIMESTAMP(6) NOT NULL,
    liters_received DECIMAL(12,3) NOT NULL,
    measurement_source VARCHAR(30) NOT NULL,
    responsible_user_id CHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    client_operation_id CHAR(36) NOT NULL UNIQUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_reception_workday FOREIGN KEY (workday_id) REFERENCES collection_workday(id),
    CONSTRAINT fk_reception_route FOREIGN KEY (route_id) REFERENCES route(id),
    CONSTRAINT fk_reception_unit FOREIGN KEY (collection_unit_id) REFERENCES collection_unit(id),
    CONSTRAINT fk_reception_delivery FOREIGN KEY (direct_delivery_id) REFERENCES direct_delivery(id),
    CONSTRAINT fk_reception_user FOREIGN KEY (responsible_user_id) REFERENCES app_user(id),
    CONSTRAINT chk_reception_liters CHECK (liters_received > 0),
    INDEX idx_reception_scope (workday_id, route_id, received_at)
);

CREATE TABLE collection_reconciliation (
    id CHAR(36) PRIMARY KEY,
    workday_id CHAR(36) NULL,
    route_id CHAR(36) NULL,
    total_field DECIMAL(12,3) NOT NULL,
    total_direct DECIMAL(12,3) NOT NULL,
    total_expected DECIMAL(12,3) NOT NULL,
    total_received DECIMAL(12,3) NOT NULL,
    difference DECIMAL(12,3) NOT NULL,
    difference_percentage DECIMAL(9,4) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_by CHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_reconciliation_workday FOREIGN KEY (workday_id) REFERENCES collection_workday(id),
    CONSTRAINT fk_reconciliation_route FOREIGN KEY (route_id) REFERENCES route(id),
    CONSTRAINT fk_reconciliation_user FOREIGN KEY (created_by) REFERENCES app_user(id),
    INDEX idx_reconciliation_scope (workday_id, route_id, created_at)
);

CREATE TABLE discrepancy_resolution (
    id CHAR(36) PRIMARY KEY,
    reconciliation_id CHAR(36) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    observations TEXT NULL,
    authorized_correction DECIMAL(12,3) NULL,
    resolved_by CHAR(36) NOT NULL,
    resolved_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_resolution_reconciliation FOREIGN KEY (reconciliation_id) REFERENCES collection_reconciliation(id),
    CONSTRAINT fk_resolution_user FOREIGN KEY (resolved_by) REFERENCES app_user(id),
    INDEX idx_resolution_reconciliation (reconciliation_id, resolved_at)
);
