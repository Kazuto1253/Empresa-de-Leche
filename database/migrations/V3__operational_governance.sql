CREATE TABLE audit_event (
    id CHAR(36) PRIMARY KEY,
    actor_user_id CHAR(36) NOT NULL,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    reason VARCHAR(500) NULL,
    before_data JSON NULL,
    after_data JSON NULL,
    correlation_id CHAR(36) NULL,
    client_operation_id CHAR(36) NULL,
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES app_user(id),
    INDEX idx_audit_entity (entity_type, entity_id, occurred_at),
    INDEX idx_audit_actor (actor_user_id, occurred_at)
);

CREATE TABLE system_parameter (
    id CHAR(36) PRIMARY KEY,
    `key` VARCHAR(160) NOT NULL,
    value TEXT NOT NULL,
    value_type VARCHAR(30) NOT NULL,
    unit VARCHAR(40) NULL,
    effective_from TIMESTAMP(6) NOT NULL,
    effective_to TIMESTAMP(6) NULL,
    status VARCHAR(30) NOT NULL,
    version BIGINT NOT NULL,
    created_by CHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    reason VARCHAR(500) NULL,
    CONSTRAINT fk_parameter_creator FOREIGN KEY (created_by) REFERENCES app_user(id),
    UNIQUE KEY uq_parameter_version (`key`, version),
    INDEX idx_parameter_effective (`key`, effective_from, effective_to)
);

CREATE TABLE provider (
    id CHAR(36) PRIMARY KEY, code VARCHAR(30) NOT NULL UNIQUE, dni VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(120) NOT NULL, last_name VARCHAR(120) NOT NULL, phone VARCHAR(30) NOT NULL,
    farm_name VARCHAR(160) NULL, status VARCHAR(30) NOT NULL, portal_user_id CHAR(36) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), version BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_provider_user FOREIGN KEY (portal_user_id) REFERENCES app_user(id), INDEX idx_provider_status(status)
);
CREATE TABLE zone (
    id CHAR(36) PRIMARY KEY, code VARCHAR(30) NOT NULL UNIQUE, name VARCHAR(120) NOT NULL, description VARCHAR(500) NULL,
    status VARCHAR(30) NOT NULL, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), version BIGINT NOT NULL DEFAULT 1
);
CREATE TABLE route (
    id CHAR(36) PRIMARY KEY, code VARCHAR(30) NOT NULL UNIQUE, name VARCHAR(120) NOT NULL, zone_id CHAR(36) NOT NULL, description VARCHAR(500) NULL,
    status VARCHAR(30) NOT NULL, valid_from DATE NOT NULL, valid_to DATE NULL, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), version BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_route_zone FOREIGN KEY (zone_id) REFERENCES zone(id), INDEX idx_route_zone(zone_id, status)
);
CREATE TABLE provider_route_assignment (
    id CHAR(36) PRIMARY KEY, provider_id CHAR(36) NOT NULL, route_id CHAR(36) NOT NULL, valid_from DATE NOT NULL, valid_to DATE NULL,
    status VARCHAR(30) NOT NULL, created_by CHAR(36) NOT NULL, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), reason VARCHAR(500) NULL, version BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_assignment_provider FOREIGN KEY (provider_id) REFERENCES provider(id), CONSTRAINT fk_assignment_route FOREIGN KEY (route_id) REFERENCES route(id), CONSTRAINT fk_assignment_creator FOREIGN KEY (created_by) REFERENCES app_user(id), INDEX idx_assignment_current(provider_id, valid_to, status)
);
CREATE TABLE collection_unit (
    id CHAR(36) PRIMARY KEY, code VARCHAR(40) NOT NULL UNIQUE, plate_or_identifier VARCHAR(80) NULL, description VARCHAR(300) NOT NULL, status VARCHAR(30) NOT NULL, version BIGINT NOT NULL DEFAULT 1
);
CREATE TABLE collection_workday (
    id CHAR(36) PRIMARY KEY, code VARCHAR(40) NOT NULL UNIQUE, route_id CHAR(36) NOT NULL, unit_id CHAR(36) NOT NULL, work_date DATE NOT NULL, planned_start_time TIME NOT NULL, planned_end_time TIME NULL, status VARCHAR(30) NOT NULL, created_by CHAR(36) NOT NULL, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), version BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_workday_route FOREIGN KEY (route_id) REFERENCES route(id), CONSTRAINT fk_workday_unit FOREIGN KEY (unit_id) REFERENCES collection_unit(id), CONSTRAINT fk_workday_creator FOREIGN KEY (created_by) REFERENCES app_user(id), INDEX idx_workday_route_date(route_id, work_date), INDEX idx_workday_unit_date(unit_id, work_date)
);
CREATE TABLE collection_workday_staff (
    workday_id CHAR(36) NOT NULL, user_id CHAR(36) NOT NULL, responsibility VARCHAR(80) NOT NULL, assigned_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), PRIMARY KEY(workday_id, user_id), FOREIGN KEY(workday_id) REFERENCES collection_workday(id), FOREIGN KEY(user_id) REFERENCES app_user(id)
);
CREATE TABLE product (
    id CHAR(36) PRIMARY KEY, code VARCHAR(40) NOT NULL UNIQUE, name VARCHAR(160) NOT NULL, category VARCHAR(60) NOT NULL, unit VARCHAR(30) NOT NULL, status VARCHAR(30) NOT NULL, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), version BIGINT NOT NULL DEFAULT 1
);
CREATE TABLE customer (
    id CHAR(36) PRIMARY KEY, code VARCHAR(40) NOT NULL UNIQUE, name VARCHAR(160) NOT NULL, document VARCHAR(40) NULL, customer_type VARCHAR(30) NOT NULL, phone VARCHAR(30) NULL, status VARCHAR(30) NOT NULL, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6), version BIGINT NOT NULL DEFAULT 1
);
CREATE TABLE product_price (
    id CHAR(36) PRIMARY KEY, product_id CHAR(36) NOT NULL, customer_type VARCHAR(30) NOT NULL, amount DECIMAL(19,4) NOT NULL, currency CHAR(3) NOT NULL, effective_from DATE NOT NULL, effective_to DATE NULL, status VARCHAR(30) NOT NULL, created_by CHAR(36) NOT NULL, created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), reason VARCHAR(500) NOT NULL, version BIGINT NOT NULL DEFAULT 1,
    FOREIGN KEY(product_id) REFERENCES product(id), FOREIGN KEY(created_by) REFERENCES app_user(id), INDEX idx_price_resolution(product_id, customer_type, effective_from, effective_to)
);
