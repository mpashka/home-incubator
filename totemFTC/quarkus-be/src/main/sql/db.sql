CREATE DATABASE totemftc;
CREATE USER totemftc WITH ENCRYPTED PASSWORD 'totemftc';
GRANT ALL PRIVILEGES ON DATABASE totemftc TO totemftc;

-- Users
CREATE TYPE user_type_enum AS ENUM ('guest', 'user', 'trainer', 'admin');

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
    user_type user_type_enum NOT NULL DEFAULT 'guest',
    training_types VARCHAR(10)[]);
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
    PRIMARY KEY (network_name,id));



--
CREATE TABLE IF NOT EXISTS training_type (
    training_type VARCHAR(10) NOT NULL PRIMARY KEY,
    name VARCHAR(20)
);

---

-- This is used only for reference and data copy to training
CREATE TABLE IF NOT EXISTS training_schedule (
    training_schedule_id SERIAL PRIMARY KEY ,
    training_time TIMESTAMP NOT NULL,
    trainer INTEGER NOT NULL REFERENCES user_info (user_id),
    training_type VARCHAR(10) NOT NULL REFERENCES training_type (training_type)
);

CREATE TABLE IF NOT EXISTS training (
    training_id SERIAL PRIMARY KEY,
    training_schedule_id INTEGER NULL REFERENCES training_schedule (training_schedule_id),
    training_time TIMESTAMP NOT NULL,
    trainer INTEGER NOT NULL REFERENCES user_info (user_id),
    training_type VARCHAR(10) NOT NULL REFERENCES training_type (training_type),
    training_comment VARCHAR(100) NULL
);

CREATE TABLE IF NOT EXISTS ticket_type (
    ticket_type_id SERIAL PRIMARY KEY ,
    ticket_name VARCHAR(20),
    ticket_cost INTEGER,
    visits INTEGER
);

CREATE TABLE IF NOT EXISTS training_ticket (
    ticket_id SERIAL PRIMARY KEY ,
    ticket_type_id INTEGER REFERENCES ticket_type,
    user_id INTEGER REFERENCES user_info(user_id),
    ticket_start DATE
);

CREATE TABLE IF NOT EXISTS training_visit (
    training_id INTEGER NOT NULL REFERENCES training (training_id),
    user_id INTEGER NOT NULL REFERENCES user_info (user_id),
    ticket_user_id INTEGER NULL REFERENCES user_info (user_id),
    visit_comment VARCHAR(200) NULL,  --
    ticket_id INTEGER NULL REFERENCES training_ticket(ticket_id),
    visit_mark_schedule BOOLEAN NOT NULL DEFAULT false,
    visit_mark_self BOOLEAN NOT NULL DEFAULT false,
    visit_mark_master BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (training_id, user_id, ticket_user_id)
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO totemftc;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO totemftc;
