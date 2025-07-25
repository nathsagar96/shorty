CREATE TABLE urls
(
    id          UUID PRIMARY KEY,
    original_url VARCHAR(2048) NOT NULL,
    short_code   VARCHAR(255) UNIQUE NOT NULL,
    active       BOOLEAN             NOT NULL DEFAULT TRUE,
    visibility   VARCHAR(50)         NOT NULL DEFAULT 'PUBLIC',
    user_id      UUID,
    created_at   TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);