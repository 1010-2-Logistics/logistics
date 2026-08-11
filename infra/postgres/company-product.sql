CREATE SCHEMA IF NOT EXISTS company_service;
CREATE SCHEMA IF NOT EXISTS product_service;
CREATE SCHEMA IF NOT EXISTS ai_service;

CREATE TABLE IF NOT EXISTS ai_service.p_ai (
    ai_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    delivery_id UUID NOT NULL,
    request_prompt TEXT,
    response_prompt TEXT,
    final_deadline TIMESTAMP,
    ai_model VARCHAR(50),
    ai_status VARCHAR(50) NOT NULL,
    call_message TEXT,
    time_ms INT,
    retry_count INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS company_service.p_company (
    company_id UUID PRIMARY KEY,
    hub_id UUID NOT NULL,
    company_manager_id BIGINT NULL,
    company_name VARCHAR(255) NOT NULL,
    company_address VARCHAR(255) NOT NULL,
    company_type VARCHAR(20) NOT NULL CHECK (company_type IN ('PRODUCER', 'RECEIVER')),
    company_status VARCHAR(20) NOT NULL CHECK (company_status IN ('PENDING', 'ACTIVE', 'FAILED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

CREATE TABLE IF NOT EXISTS product_service.p_product (
    product_id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    product_name VARCHAR(20) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS UQ_P_PRODUCT_COMPANY_ID_PRODUCT_NAME
ON product_service.p_product (company_id, product_name)
WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS UQ_P_COMPANY_HUB_ID_COMPANY_NAME
ON company_service.p_company (hub_id, company_name)
WHERE deleted_at IS NULL;