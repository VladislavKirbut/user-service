--liquibase formatted sql

--changeset vkirbut:003
CREATE INDEX idx_users_name
ON users(name);

CREATE INDEX idx_users_surname
ON users(surname);

CREATE INDEX idx_payment_cards_user_active
ON payment_cards(user_id, active);