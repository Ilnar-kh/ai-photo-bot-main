ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS offer_shown_at     timestamptz NULL,
  ADD COLUMN IF NOT EXISTS purchased_at       timestamptz NULL,
  ADD COLUMN IF NOT EXISTS followup30_sent_at timestamptz NULL,
  ADD COLUMN IF NOT EXISTS followup24_sent_at timestamptz NULL;