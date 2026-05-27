-- liquibase formatted sql

-- changeset liquibase:001-1 author:evg
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     username VARCHAR(255) NOT NULL,
                                     password VARCHAR(255) NOT NULL,
                                     first_name VARCHAR(16) NOT NULL,
                                     last_name VARCHAR(16) NOT NULL,
                                     phone VARCHAR(50) NOT NULL,
                                     role VARCHAR(10) NOT NULL,
                                     image VARCHAR(255)
);

-- changeset liquibase:001-2 author:evg
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);

-- changeset liquibase:001-3 author:evg
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'));