CREATE TABLE app_role (
    code VARCHAR(40) PRIMARY KEY,
    description VARCHAR(160) NOT NULL
);

INSERT INTO app_role(code, description) VALUES
('ADMINISTRADOR_GENERAL', 'Administración general del sistema'),
('PERSONAL_PLANTA', 'Operación interna de planta'),
('ACOPIADOR', 'Acopio de leche en campo'),
('PROVEEDOR', 'Consulta de información propia');

CREATE TABLE app_user (
    id CHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_code VARCHAR(40) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    provider_id CHAR(36) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_app_user_role FOREIGN KEY (role_code) REFERENCES app_role(code)
);

CREATE TABLE processed_client_operation (
    operation_id CHAR(36) PRIMARY KEY,
    actor_user_id CHAR(36) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    server_version BIGINT NOT NULL,
    processed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_processed_client_operation_actor FOREIGN KEY (actor_user_id) REFERENCES app_user(id),
    INDEX idx_processed_client_operation_aggregate (aggregate_type, aggregate_id)
);
