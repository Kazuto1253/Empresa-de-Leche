CREATE TABLE production_batch (
    id CHAR(36) PRIMARY KEY,
    batch_code VARCHAR(60) NOT NULL UNIQUE,
    production_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RECORDED',
    created_by CHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    notes TEXT NULL,
    CONSTRAINT fk_production_batch_user FOREIGN KEY (created_by) REFERENCES app_user(id),
    INDEX idx_production_batch_date (production_date, status)
);

CREATE TABLE production_input (
    id CHAR(36) PRIMARY KEY,
    batch_id CHAR(36) NOT NULL,
    input_type VARCHAR(40) NOT NULL,
    source_id CHAR(36) NULL,
    quantity DECIMAL(14,3) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    CONSTRAINT fk_production_input_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id),
    CONSTRAINT chk_production_input_quantity CHECK (quantity > 0),
    INDEX idx_production_input_batch (batch_id)
);

CREATE TABLE production_output (
    id CHAR(36) PRIMARY KEY,
    batch_id CHAR(36) NOT NULL,
    product_id CHAR(36) NOT NULL,
    quantity DECIMAL(14,3) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    CONSTRAINT fk_production_output_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id),
    CONSTRAINT fk_production_output_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT chk_production_output_quantity CHECK (quantity > 0),
    INDEX idx_production_output_batch (batch_id, product_id)
);

CREATE TABLE inventory_movement (
    id CHAR(36) PRIMARY KEY,
    product_id CHAR(36) NOT NULL,
    movement_type VARCHAR(10) NOT NULL,
    quantity DECIMAL(14,3) NOT NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_id CHAR(36) NOT NULL,
    created_by CHAR(36) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_inventory_user FOREIGN KEY (created_by) REFERENCES app_user(id),
    CONSTRAINT chk_inventory_type CHECK (movement_type IN ('IN','OUT')),
    CONSTRAINT chk_inventory_quantity CHECK (quantity > 0),
    UNIQUE KEY uq_inventory_source (product_id, source_type, source_id),
    INDEX idx_inventory_product_date (product_id, occurred_at)
);

CREATE TABLE sale (
    id CHAR(36) PRIMARY KEY,
    customer_id CHAR(36) NOT NULL,
    sold_at TIMESTAMP(6) NOT NULL,
    seller_user_id CHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    currency CHAR(3) NOT NULL,
    total DECIMAL(19,4) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_sale_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_sale_user FOREIGN KEY (seller_user_id) REFERENCES app_user(id),
    CONSTRAINT chk_sale_total CHECK (total >= 0),
    INDEX idx_sale_date_status (sold_at, status),
    INDEX idx_sale_customer (customer_id, sold_at)
);

CREATE TABLE sale_item (
    id CHAR(36) PRIMARY KEY,
    sale_id CHAR(36) NOT NULL,
    product_id CHAR(36) NOT NULL,
    quantity DECIMAL(14,3) NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    subtotal DECIMAL(19,4) NOT NULL,
    price_id CHAR(36) NULL,
    CONSTRAINT fk_sale_item_sale FOREIGN KEY (sale_id) REFERENCES sale(id),
    CONSTRAINT fk_sale_item_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_sale_item_price FOREIGN KEY (price_id) REFERENCES product_price(id),
    CONSTRAINT chk_sale_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_sale_item_price CHECK (unit_price > 0),
    INDEX idx_sale_item_sale (sale_id, product_id)
);
