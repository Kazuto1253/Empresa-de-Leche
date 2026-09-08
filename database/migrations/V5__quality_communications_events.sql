CREATE TABLE quality_parameter (
    id CHAR(36) PRIMARY KEY,
    `key` VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    unit VARCHAR(40) NULL,
    value_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    metadata JSON NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE quality_analysis (
    id CHAR(36) PRIMARY KEY,
    provider_id CHAR(36) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_id CHAR(36) NOT NULL,
    sample_reference VARCHAR(120) NULL,
    analyzed_at TIMESTAMP(6) NOT NULL,
    analyst_user_id CHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RECORDED',
    observations TEXT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_quality_analysis_provider FOREIGN KEY (provider_id) REFERENCES provider(id),
    CONSTRAINT fk_quality_analysis_analyst FOREIGN KEY (analyst_user_id) REFERENCES app_user(id),
    UNIQUE KEY uq_quality_source (source_type, source_id),
    INDEX idx_quality_provider_date (provider_id, analyzed_at),
    INDEX idx_quality_source (source_type, source_id)
);

CREATE TABLE quality_test_result (
    id CHAR(36) PRIMARY KEY,
    analysis_id CHAR(36) NOT NULL,
    parameter_id CHAR(36) NOT NULL,
    decimal_value DECIMAL(19,6) NULL,
    text_value VARCHAR(500) NULL,
    unit VARCHAR(40) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RECORDED',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_quality_result_analysis FOREIGN KEY (analysis_id) REFERENCES quality_analysis(id),
    CONSTRAINT fk_quality_result_parameter FOREIGN KEY (parameter_id) REFERENCES quality_parameter(id),
    UNIQUE KEY uq_quality_result_parameter (analysis_id, parameter_id),
    INDEX idx_quality_result_analysis (analysis_id)
);

CREATE TABLE quality_classification (
    id CHAR(36) PRIMARY KEY,
    analysis_id CHAR(36) NOT NULL,
    classification VARCHAR(40) NOT NULL,
    rule_version VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL,
    classified_by CHAR(36) NOT NULL,
    classified_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    observations TEXT NULL,
    CONSTRAINT fk_quality_classification_analysis FOREIGN KEY (analysis_id) REFERENCES quality_analysis(id),
    CONSTRAINT fk_quality_classification_user FOREIGN KEY (classified_by) REFERENCES app_user(id),
    INDEX idx_quality_classification_analysis (analysis_id, classified_at)
);

CREATE TABLE quality_incident (
    id CHAR(36) PRIMARY KEY,
    provider_id CHAR(36) NOT NULL,
    analysis_id CHAR(36) NULL,
    classification_id CHAR(36) NULL,
    category VARCHAR(80) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    occurred_at TIMESTAMP(6) NOT NULL,
    created_by CHAR(36) NOT NULL,
    closed_at TIMESTAMP(6) NULL,
    CONSTRAINT fk_quality_incident_provider FOREIGN KEY (provider_id) REFERENCES provider(id),
    CONSTRAINT fk_quality_incident_analysis FOREIGN KEY (analysis_id) REFERENCES quality_analysis(id),
    CONSTRAINT fk_quality_incident_classification FOREIGN KEY (classification_id) REFERENCES quality_classification(id),
    CONSTRAINT fk_quality_incident_user FOREIGN KEY (created_by) REFERENCES app_user(id),
    INDEX idx_quality_incident_provider (provider_id, occurred_at, status),
    INDEX idx_quality_incident_analysis (analysis_id)
);

CREATE TABLE quality_measure (
    id CHAR(36) PRIMARY KEY,
    incident_id CHAR(36) NOT NULL,
    measure_type VARCHAR(80) NOT NULL,
    description TEXT NOT NULL,
    starts_at TIMESTAMP(6) NOT NULL,
    ends_at TIMESTAMP(6) NULL,
    status VARCHAR(30) NOT NULL,
    responsible_user_id CHAR(36) NOT NULL,
    approved_by CHAR(36) NULL,
    observations TEXT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_quality_measure_incident FOREIGN KEY (incident_id) REFERENCES quality_incident(id),
    CONSTRAINT fk_quality_measure_responsible FOREIGN KEY (responsible_user_id) REFERENCES app_user(id),
    CONSTRAINT fk_quality_measure_approver FOREIGN KEY (approved_by) REFERENCES app_user(id),
    INDEX idx_quality_measure_incident (incident_id, starts_at)
);

CREATE TABLE technical_follow_up (
    id CHAR(36) PRIMARY KEY,
    provider_id CHAR(36) NOT NULL,
    incident_id CHAR(36) NULL,
    follow_up_type VARCHAR(80) NOT NULL,
    scheduled_at TIMESTAMP(6) NULL,
    performed_at TIMESTAMP(6) NULL,
    responsible_user_id CHAR(36) NOT NULL,
    result TEXT NULL,
    notes TEXT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_follow_up_provider FOREIGN KEY (provider_id) REFERENCES provider(id),
    CONSTRAINT fk_follow_up_incident FOREIGN KEY (incident_id) REFERENCES quality_incident(id),
    CONSTRAINT fk_follow_up_user FOREIGN KEY (responsible_user_id) REFERENCES app_user(id),
    INDEX idx_follow_up_provider (provider_id, scheduled_at)
);

CREATE TABLE provider_notification (
    id CHAR(36) PRIMARY KEY,
    provider_id CHAR(36) NOT NULL,
    notification_type VARCHAR(60) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    source_type VARCHAR(60) NULL,
    source_id CHAR(36) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
    published_at TIMESTAMP(6) NULL,
    created_by CHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_notification_provider FOREIGN KEY (provider_id) REFERENCES provider(id),
    CONSTRAINT fk_notification_user FOREIGN KEY (created_by) REFERENCES app_user(id),
    INDEX idx_notification_provider (provider_id, status, published_at)
);

CREATE TABLE communication_event (
    id CHAR(36) PRIMARY KEY,
    event_type VARCHAR(60) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    starts_at TIMESTAMP(6) NOT NULL,
    ends_at TIMESTAMP(6) NULL,
    location VARCHAR(200) NULL,
    audience VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP(6) NULL,
    created_by CHAR(36) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_event_user FOREIGN KEY (created_by) REFERENCES app_user(id),
    INDEX idx_event_audience_status (audience, status, starts_at)
);

CREATE TABLE event_attendance (
    event_id CHAR(36) NOT NULL,
    provider_id CHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL,
    marked_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    marked_by CHAR(36) NOT NULL,
    marking_method VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    PRIMARY KEY (event_id, provider_id),
    CONSTRAINT fk_attendance_event FOREIGN KEY (event_id) REFERENCES communication_event(id),
    CONSTRAINT fk_attendance_provider FOREIGN KEY (provider_id) REFERENCES provider(id),
    CONSTRAINT fk_attendance_user FOREIGN KEY (marked_by) REFERENCES app_user(id)
);
