ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS identity_path TEXT;
