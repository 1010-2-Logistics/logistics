CREATE SCHEMA IF NOT EXISTS hub_service;

CREATE TABLE IF NOT EXISTS hub_service.p_hub (
    hub_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
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