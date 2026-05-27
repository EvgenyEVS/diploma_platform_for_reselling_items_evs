-- liquibase formatted sql

-- changeset liquibase:002-1 author:evg
CREATE TABLE IF NOT EXISTS ads (
                                   pk SERIAL PRIMARY KEY,
                                   author_id INTEGER NOT NULL,
                                   title VARCHAR(32) NOT NULL,
                                   price INTEGER NOT NULL,
                                   description VARCHAR(255) NOT NULL,
                                   image VARCHAR(255)
);

-- changeset liquibase:002-2 author:evg
ALTER TABLE ads ADD CONSTRAINT fk_ads_author
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE;

-- changeset liquibase:002-3 author:evg
ALTER TABLE ads ADD CONSTRAINT chk_ads_price CHECK (price >= 0 AND price <= 10000000);

-- changeset liquibase:002-4 author:evg
ALTER TABLE ads ADD CONSTRAINT chk_ads_title_length CHECK (LENGTH(title) BETWEEN 4 AND 32);

-- changeset liquibase:002-5 author:evg
ALTER TABLE ads ADD CONSTRAINT chk_ads_description_length CHECK (LENGTH(description) BETWEEN 8 AND 255);