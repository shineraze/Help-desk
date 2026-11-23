-- Вариант A «База знаний» (раздел 42).
-- Две новые таблицы, связь решённой заявки со статьёй.

SET client_encoding = 'UTF8';

CREATE TABLE article_categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE knowledge_articles (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(200) NOT NULL,
    content          TEXT NOT NULL,
    category_id      BIGINT NOT NULL REFERENCES article_categories(id),
    author_id        BIGINT NOT NULL REFERENCES users(id),
    source_ticket_id BIGINT REFERENCES tickets(id),
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT article_title_not_blank
        CHECK (length(trim(title)) >= 3)
);

CREATE INDEX idx_articles_category
    ON knowledge_articles(category_id);

CREATE INDEX idx_articles_ticket
    ON knowledge_articles(source_ticket_id);

INSERT INTO article_categories(name, description) VALUES
('Сеть', 'Wi-Fi, VPN, доступ в Интернет'),
('Учётные записи', 'Пароли, права доступа'),
('Оборудование', 'ПК, принтеры, периферия');
