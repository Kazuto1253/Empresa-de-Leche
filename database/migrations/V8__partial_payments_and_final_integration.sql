ALTER TABLE payment ADD INDEX idx_payment_settlement_fk (settlement_id);
ALTER TABLE payment DROP INDEX uq_payment_settlement;

ALTER TABLE payment
    ADD COLUMN notes VARCHAR(500) NULL AFTER reference,
    ADD COLUMN cancelled_at TIMESTAMP(6) NULL AFTER status,
    ADD COLUMN cancelled_by CHAR(36) NULL AFTER cancelled_at,
    ADD COLUMN cancellation_reason VARCHAR(500) NULL AFTER cancelled_by,
    ADD CONSTRAINT fk_payment_cancelled_by FOREIGN KEY (cancelled_by) REFERENCES app_user(id),
    ADD INDEX idx_payment_settlement_status (settlement_id, status, paid_at);

CREATE TABLE weekly_collection_closure_revision (
    id CHAR(36) PRIMARY KEY,
    closure_id CHAR(36) NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    changed_by CHAR(36) NOT NULL,
    changed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    snapshot JSON NOT NULL,
    CONSTRAINT fk_closure_revision_closure FOREIGN KEY (closure_id) REFERENCES weekly_collection_closure(id),
    CONSTRAINT fk_closure_revision_user FOREIGN KEY (changed_by) REFERENCES app_user(id),
    INDEX idx_closure_revision_history (closure_id, changed_at)
);
