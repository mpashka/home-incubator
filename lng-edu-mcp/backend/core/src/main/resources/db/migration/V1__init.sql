-- @tag:domain-model @tag:request-id @tag:reading-block
-- Initial schema for the MVP vertical slice. Source of truth after implementation
-- (see AGENTS.md rule 6). Mirrors docs/db-schema.md exactly.

-- gen_random_uuid() lives in pgcrypto on older PostgreSQL; enable it explicitly.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Local learning profiles (no production auth in the first phase).
CREATE TABLE users (
    id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name text        NOT NULL,
    timezone     text        NOT NULL,
    created_at   timestamptz NOT NULL DEFAULT now()
);

-- Per user+language proficiency and adaptive reading-block parameters.
CREATE TABLE user_language_skills (
    id                   uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    language             text        NOT NULL CHECK (language IN ('sr', 'en')),
    level                text        NOT NULL,
    block_min_words      int         NOT NULL,
    block_max_words      int         NOT NULL,
    target_unknown_ratio numeric     NULL,
    updated_at           timestamptz NOT NULL,
    CONSTRAINT uq_user_language UNIQUE (user_id, language)
);

-- Book metadata on a specific language (kept light; text lives in book_texts).
CREATE TABLE books (
    id         uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    title      text        NOT NULL,
    language   text        NOT NULL CHECK (language IN ('sr', 'en')),
    author     text        NULL,
    source     text        NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

-- Full book text as a single large block, 1:1 with books.
CREATE TABLE book_texts (
    book_id      uuid PRIMARY KEY REFERENCES books (id) ON DELETE CASCADE,
    content      text NOT NULL,
    length_chars int  NOT NULL
);

-- Reading position of a user in a book (character offset, one row per user+book).
CREATE TABLE reading_progress (
    id            uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    book_id       uuid        NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    position_char int         NOT NULL DEFAULT 0,
    updated_at    timestamptz NOT NULL,
    CONSTRAINT uq_user_book UNIQUE (user_id, book_id)
);

-- Learning session: interval of studying a book.
CREATE TABLE learning_sessions (
    id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    book_id     uuid        NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    started_at  timestamptz NOT NULL,
    finished_at timestamptz NULL,
    request_id  text        NOT NULL,
    CONSTRAINT uq_session_request_id UNIQUE (request_id)
);

-- User vocabulary entry: a word in the studied language and its current status.
CREATE TABLE vocabulary_items (
    id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    language     text        NOT NULL CHECK (language IN ('sr', 'en')),
    lemma        text        NOT NULL,
    status       text        NOT NULL,
    last_context text        NULL,
    last_seen_at timestamptz NOT NULL,
    created_at   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_language_lemma UNIQUE (user_id, language, lemma)
);

-- Event log per word (seen, marked unknown, review outcome).
CREATE TABLE word_events (
    id                 uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    vocabulary_item_id uuid        NOT NULL REFERENCES vocabulary_items (id) ON DELETE CASCADE,
    session_id         uuid        NULL REFERENCES learning_sessions (id) ON DELETE SET NULL,
    event_type         text        NOT NULL,
    context            text        NULL,
    occurred_at        timestamptz NOT NULL,
    request_id         text        NULL,
    CONSTRAINT uq_word_event_request_id UNIQUE (request_id)
);
