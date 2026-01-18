ALTER TABLE orders
    ADD COLUMN gender VARCHAR(10),
    ADD COLUMN training_request_id VARCHAR(255),
    ADD COLUMN lora_trained_at TIMESTAMPTZ,
    ADD COLUMN lora_status VARCHAR(32) NOT NULL DEFAULT 'NONE',
    ADD COLUMN lora_config_url TEXT;

UPDATE orders SET lora_status = 'NONE' WHERE lora_status IS NULL;

ALTER TABLE orders
    ALTER COLUMN lora_status DROP DEFAULT;
