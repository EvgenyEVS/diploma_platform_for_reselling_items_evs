-- liquibase formatted sql

-- changeset liquibase:003-1 author:evg
CREATE TABLE IF NOT EXISTS comments (
                                        pk SERIAL PRIMARY KEY,
                                        author_id INTEGER NOT NULL,
                                        ad_id INTEGER NOT NULL,
                                        text VARCHAR(64) NOT NULL,
                                        created_at BIGINT NOT NULL
);

-- changeset liquibase:003-2 author:evg
ALTER TABLE comments ADD CONSTRAINT fk_comments_author
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE;

-- changeset liquibase:003-3 author:evg
ALTER TABLE comments ADD CONSTRAINT fk_comments_ad
    FOREIGN KEY (ad_id) REFERENCES ads(pk) ON DELETE CASCADE;

-- changeset liquibase:003-4 author:evg
ALTER TABLE comments ADD CONSTRAINT chk_comments_text_length CHECK (LENGTH(text) BETWEEN 8 AND 64);