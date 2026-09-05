-- Признак отложенной языковой доработки.
-- Замысел: docs/implementation/db-schema.md, раздел «Отложенная доработка».
-- @tag:word-forms @tag:part-of-speech

-- Слово перенесено и им уже можно пользоваться, но по нему остались известные
-- языковые пробелы: не разделены омонимы, не определена часть речи, не разделены
-- значения, нет достоверных ударений в словоформах. Оттачивание качества отложено;
-- этот признак позволяет потом найти такие слова запросом и вернуться к ним.
alter table word
    add column needs_language_review boolean not null default false,
    -- Через запятую: чем именно слово требует внимания.
    add column review_reason          text;

comment on column word.needs_language_review is
    'Слово используется, но по нему есть отложенные языковые пробелы (см. review_reason)';

-- Очередь на доработку — по этому признаку её и разбирают.
create index word_review_idx on word (needs_language_review) where needs_language_review;
