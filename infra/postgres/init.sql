CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS slack_service;
CREATE SCHEMA IF NOT EXISTS hub_service;
CREATE SCHEMA IF NOT EXISTS company_service;
CREATE SCHEMA IF NOT EXISTS product_service;
CREATE SCHEMA IF NOT EXISTS inventory_service;
CREATE SCHEMA IF NOT EXISTS order_service;
CREATE SCHEMA IF NOT EXISTS delivery_service;

CREATE TABLE IF NOT EXISTS hub_service.p_hub (
    hub_id      UUID           DEFAULT gen_random_uuid() PRIMARY KEY,
    hub_name    VARCHAR(100)   NOT NULL,
    hub_address VARCHAR(255)   NOT NULL,
    latitude    DECIMAL(10, 7) NOT NULL,
    longitude   DECIMAL(10, 7) NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  BIGINT         NOT NULL,
    updated_at  TIMESTAMP      NULL,
    updated_by  BIGINT         NULL,
    deleted_at  TIMESTAMP      NULL,
    deleted_by  BIGINT         NULL
);

CREATE TABLE IF NOT EXISTS delivery_service.p_delivery_manager (
    delivery_manager_id BIGINT PRIMARY KEY,
    hub_id UUID,
    slack_id VARCHAR(255) NOT NULL,
    manager_type VARCHAR(50) NOT NULL,
    delivery_sequence INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);
