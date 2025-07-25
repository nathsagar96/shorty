CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    email          VARCHAR(255) UNIQUE NOT NULL,
    first_name     VARCHAR(255)        NOT NULL,
    last_name      VARCHAR(255),
    password_hash  VARCHAR(255)        NOT NULL,
    enabled        BOOLEAN             NOT NULL DEFAULT TRUE,
    account_expired BOOLEAN             NOT NULL DEFAULT FALSE,
    account_locked BOOLEAN             NOT NULL DEFAULT FALSE,
    credentials_expired BOOLEAN             NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP
);