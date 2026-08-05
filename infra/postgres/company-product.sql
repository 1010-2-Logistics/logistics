CREATE SCHEMA IF NOT EXISTS company_service;
CREATE SCHEMA IF NOT EXISTS product_service;

CREATE TABLE IF NOT EXISTS company_service.p_company_service(
    company_id UUID PRIMARY KEY,
    hub_id UUID NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    company_address VARCHAR(255) NOT NULL,
    company_type VARCHAR(20) NOT NULL CHECK (company_type IN('PRODUCER', 'RECEIVER')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
    );