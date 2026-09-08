-- RF-34. V1 remains immutable. MySQL 8.0.16+ enforces CHECK constraints.
ALTER TABLE app_role ADD CONSTRAINT chk_official_role CHECK
    (code IN ('ADMINISTRADOR_GENERAL', 'PERSONAL_PLANTA', 'ACOPIADOR', 'PROVEEDOR'));
ALTER TABLE app_user ADD CONSTRAINT chk_user_active CHECK (active IN (0, 1));

CREATE TABLE auth_session (
    id CHAR(36) PRIMARY KEY,
    family_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    access_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL UNIQUE,
    refresh_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL UNIQUE,
    created_at BIGINT NOT NULL,
    access_expires_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    revoked_at BIGINT NULL,
    CONSTRAINT fk_auth_session_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT chk_session_expiry CHECK (expires_at > created_at AND access_expires_at <= expires_at),
    INDEX idx_auth_session_family (family_id),
    INDEX idx_auth_session_user (user_id),
    INDEX idx_auth_session_expiry (expires_at)
);
