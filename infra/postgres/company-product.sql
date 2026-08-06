CREATE SCHEMA IF NOT EXISTS company_service;
CREATE SCHEMA IF NOT EXISTS product_service;

CREATE TABLE IF NOT EXISTS company_service.p_company (
    company_id UUID PRIMARY KEY,
    hub_id UUID NOT NULL,
    company_manager_id BIGINT NULL,
    company_name VARCHAR(255) NOT NULL,
    company_address VARCHAR(255) NOT NULL,
    company_type VARCHAR(20) NOT NULL CHECK (company_type IN ('PRODUCER', 'RECEIVER')),
    company_status VARCHAR(20) NOT NULL CHECK (company_status IN ('PENDING', 'ACTIVE', 'INACTIVE', 'DELETED')),
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
    product_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS UQ_P_COMPANY_PRODUCT_NAME_ACTIVE
ON p_product (company_id, product_name)
WHERE deleted_at IS NULL;