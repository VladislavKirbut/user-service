--liquibase formatted sql

--changeset vkirbut:002
CREATE TABLE payment_cards(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    number VARCHAR(19) NOT NULL UNIQUE,
    holder VARCHAR(100) NOT NULL,
    expiration_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    updated_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);