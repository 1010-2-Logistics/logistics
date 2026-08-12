CREATE SCHEMA IF NOT EXISTS ai_service;

-- AiHistory는 BaseEntity를 상속하지 않아 감사 컬럼이 없다.
CREATE TABLE IF NOT EXISTS ai_service.p_ai (
    ai_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    delivery_id UUID NOT NULL,
    request_prompt TEXT,
    response_prompt TEXT,
    final_deadline TIMESTAMP,
    ai_model VARCHAR(255),
    ai_status VARCHAR(20) NOT NULL,
    call_message TEXT,
    time_ms INTEGER,
    retry_count INTEGER NOT NULL
    );
