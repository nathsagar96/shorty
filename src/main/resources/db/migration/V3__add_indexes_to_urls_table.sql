CREATE UNIQUE INDEX idx_user_email ON users (email);
CREATE UNIQUE INDEX idx_urls_short_code ON urls (short_code);
CREATE INDEX idx_urls_user_id ON urls (user_id);
CREATE INDEX idx_urls_created_at ON urls (created_at);