-- @tag:domain-model @tag:reading-block @tag:request-id
-- Reading-event log: one row per delivered block whose result was recorded.
-- Makes comprehension durable and lets daily stats derive chars/blocks read.

CREATE TABLE reading_events (
    id            uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    book_id       uuid        NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    session_id    uuid        NULL REFERENCES learning_sessions (id) ON DELETE SET NULL,
    start_offset  int         NOT NULL,
    end_offset    int         NOT NULL,
    chars_read    int         NOT NULL,
    comprehension text        NOT NULL CHECK (comprehension IN ('understood', 'partial', 'unclear')),
    request_id    text        NULL,
    occurred_at   timestamptz NOT NULL,
    CONSTRAINT uq_reading_event_request_id UNIQUE (request_id)
);

-- Daily-stats aggregation is by user within a UTC instant window.
CREATE INDEX idx_reading_events_user_occurred ON reading_events (user_id, occurred_at);
