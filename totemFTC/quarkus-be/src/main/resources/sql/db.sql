CREATE DATABASE totemftc;
CREATE USER totemftc WITH ENCRYPTED PASSWORD 'totemftc';
GRANT ALL PRIVILEGES ON DATABASE totemftc TO totemftc;

-- Users
CREATE TYPE user_type_enum AS ENUM ('user', 'trainer', 'admin');
CREATE TYPE mark_type_enum AS ENUM ('on', 'off', 'unmark');

CREATE TABLE IF NOT EXISTS user_type_description (
    user_type user_type_enum NOT NULL PRIMARY KEY,
    name VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS user_info (
    user_id SERIAL PRIMARY KEY,
    first_name VARCHAR(30) NULL,
    last_name VARCHAR(30) NULL,
    nick_name VARCHAR(30) NULL,
    primary_image INTEGER NULL,
    user_types user_type_enum[],
    user_training_types VARCHAR(10)[]        -- this is for user_types=='trainer'
);
-- FOREIGN KEY (EACH ELEMENT OF training_types) REFERENCES training_type,
CREATE TABLE IF NOT EXISTS user_email (
    email VARCHAR(30) NOT NULL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_info (user_id),
    confirmed boolean NOT NULL);
CREATE TABLE IF NOT EXISTS user_phone (
    phone VARCHAR(14) NOT NULL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_info (user_id),
    confirmed boolean NOT NULL);
CREATE TABLE IF NOT EXISTS user_image (
    image_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_info (user_id),
    image bytea,
    content_type VARCHAR(20) NOT NULL);
CREATE TABLE IF NOT EXISTS user_social_network (
    -- facebook, okru, e.t.c. See AuthProvider#name
    network_name VARCHAR(10) NOT NULL,
    -- User id in social network. Specific to network
    id VARCHAR(30) NOT NULL,
    user_id INTEGER NOT NULL REFERENCES user_info (user_id),
    link VARCHAR(400) NULL,
    display_name VARCHAR(60) NULL,
    PRIMARY KEY (network_name,id));

CREATE TABLE IF NOT EXISTS user_session (
    session_id VARCHAR(20) NOT NULL PRIMARY KEY, -- See Utils.SESSION_ID_LENGTH
    user_id INTEGER NOT NULL REFERENCES user_info (user_id),
    last_update TIMESTAMP WITH TIME ZONE NOT NULL
);

--
CREATE TABLE IF NOT EXISTS training_type (
    training_type VARCHAR(10) NOT NULL PRIMARY KEY,
    name VARCHAR(20),
    default_cost INTEGER NOT NULL
);

---

-- This is used only for reference and data copy to training
CREATE TABLE IF NOT EXISTS training_schedule (
    training_schedule_id SERIAL PRIMARY KEY,
    training_time TIMESTAMP NOT NULL,
    trainer_id INTEGER NOT NULL REFERENCES user_info (user_id),
    training_type VARCHAR(10) NOT NULL REFERENCES training_type (training_type)
);

CREATE TABLE IF NOT EXISTS training (
    training_id SERIAL PRIMARY KEY,
    training_schedule_id INTEGER NULL REFERENCES training_schedule (training_schedule_id),
    training_time TIMESTAMP NOT NULL,
    trainer_id INTEGER NOT NULL REFERENCES user_info (user_id),
    training_type VARCHAR(10) NOT NULL REFERENCES training_type (training_type),
    training_comment VARCHAR(100) NULL
);

CREATE TABLE IF NOT EXISTS ticket_type (
    ticket_type_id SERIAL PRIMARY KEY,
    ticket_training_types VARCHAR(10)[] NOT NULL,
    ticket_name VARCHAR(20)  NOT NULL,
    ticket_cost INTEGER NOT NULL,
    ticket_visits INTEGER NOT NULL,
    ticket_days INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS training_ticket (
    ticket_id SERIAL PRIMARY KEY,
    ticket_type_id INTEGER REFERENCES ticket_type,
    user_id INTEGER REFERENCES user_info(user_id),
    ticket_buy TIMESTAMP NOT NULL,
    ticket_start DATE NULL,
    ticket_end DATE NULL,
    training_visits INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS training_visit (
    training_id INTEGER NOT NULL REFERENCES training (training_id),
    user_id INTEGER NOT NULL REFERENCES user_info (user_id),
    -- ticket_user_id is used to set payer if ticker_id is null
    ticket_user_id INTEGER NULL REFERENCES user_info (user_id),
    ticket_id INTEGER NULL REFERENCES training_ticket(ticket_id),
    visit_mark_schedule BOOLEAN NOT NULL DEFAULT false,
    visit_mark_self mark_type_enum NOT NULL DEFAULT 'unmark'::mark_type_enum,
    visit_mark_master mark_type_enum NOT NULL DEFAULT 'unmark'::mark_type_enum,
    visit_comment VARCHAR(200) NULL,
    PRIMARY KEY (training_id, user_id)
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO totemftc;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO totemftc;
