-- liquibase formatted sql

-- changeset liquibase:004-1 author:evg
-- Индексы для таблицы users
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- changeset liquibase:004-2 author:evg
-- Индексы для таблицы ads
CREATE INDEX IF NOT EXISTS idx_ads_author_id ON ads(author_id);
CREATE INDEX IF NOT EXISTS idx_ads_price ON ads(price);

-- changeset liquibase:004-3 author:evg
-- Индексы для таблицы comments
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comments(author_id);
CREATE INDEX IF NOT EXISTS idx_comments_ad_id ON comments(ad_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at);

-- changeset liquibase:004-4 author:evg
-- Составные индексы для частых запросов
CREATE INDEX IF NOT EXISTS idx_ads_author_created ON ads(author_id, pk DESC);
CREATE INDEX IF NOT EXISTS idx_comments_ad_created ON comments(ad_id, created_at DESC);