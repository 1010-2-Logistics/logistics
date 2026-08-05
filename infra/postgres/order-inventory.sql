CREATE SCHEMA IF NOT EXISTS inventory_service;
CREATE SCHEMA IF NOT EXISTS order_service;

CREATE TABLE IF NOT EXISTS order_service.p_order
(
    order_id         UUID PRIMARY KEY,
    start_company_id UUID         NOT NULL,
    end_company_id   UUID         NOT NULL,
    delivery_id      UUID         NOT NULL,
    product_id       UUID         NOT NULL,
    quantity         INTEGER      NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    request          VARCHAR(500) NOT NULL,

    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       BIGINT,
    updated_at       TIMESTAMP,
    updated_by       BIGINT,
    deleted_at       TIMESTAMP,
    deleted_by       BIGINT,

    CONSTRAINT chk_order_status
    CHECK (status IN ('CREATED', 'CANCELED'))
    );

CREATE TABLE IF NOT EXISTS inventory_service.p_inventory
(
    inventory_id UUID PRIMARY KEY,
    product_id   UUID      NOT NULL,
    hub_id       UUID      NOT NULL,
    stock        INTEGER   NOT NULL,

    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   BIGINT,
    updated_at   TIMESTAMP,
    updated_by   BIGINT,
    deleted_at   TIMESTAMP,
    deleted_by   BIGINT,

    CONSTRAINT uk_inventory_product_hub
    UNIQUE (product_id, hub_id)
    );