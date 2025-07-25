ALTER TABLE urls
    ADD COLUMN expires_at TIMESTAMP,
    ADD COLUMN click_limit INTEGER NOT NULL DEFAULT -1,
    ADD COLUMN click_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN description VARCHAR(500),
    ADD COLUMN password_protected BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN password_hash VARCHAR(255);

CREATE INDEX idx_url_expires_at ON urls (expires_at);
CREATE INDEX idx_url_active_expires ON urls (active, expires_at);
CREATE INDEX idx_url_click_limit ON urls (click_limit, click_count);
