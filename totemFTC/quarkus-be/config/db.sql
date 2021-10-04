-- Users
CREATE TABLE IF NOT EXISTS user_type (
    user_type VARCHAR(10) NOT NULL PRIMARY KEY,
    name VARCHAR(20)
);
INSERT INTO user_type (user_type, name) VALUES
('guest',   'Гость'),
('user',    'Посетитель'),  -- Allow payments through app
('trainer', 'Тренер'),
('admin',   'Администратор');

CREATE TABLE IF NOT EXISTS user_info
    (user_id SERIAL PRIMARY KEY, first_name VARCHAR(30) NULL, last_name VARCHAR(30) NULL, nick_name VARCHAR(30) NULL,
    primary_image INTEGER NULL, user_type VARCHAR(10) NOT NULL REFERENCES user_type(user_type) DEFAULT 'guest');
CREATE TABLE IF NOT EXISTS user_email
    (email VARCHAR(30) NOT NULL PRIMARY KEY,user_id INTEGER NOT NULL REFERENCES user_info (user_id),confirmed boolean NOT NULL);
CREATE TABLE IF NOT EXISTS user_phone
    (phone VARCHAR(14) NOT NULL PRIMARY KEY,user_id INTEGER NOT NULL REFERENCES user_info (user_id), confirmed boolean NOT NULL);
CREATE TABLE IF NOT EXISTS user_image
    (image_id SERIAL PRIMARY KEY,user_id INTEGER NOT NULL REFERENCES user_info (user_id),image bytea,content_type VARCHAR(20) NOT NULL);
CREATE TABLE IF NOT EXISTS user_social_network
    (network_id VARCHAR(10) NOT NULL,id VARCHAR(30) NOT NULL,user_id INTEGER NOT NULL REFERENCES user_info (user_id),link VARCHAR(400) NULL,PRIMARY KEY (network_id,id));



--
CREATE TABLE IF NOT EXISTS training_type (
    training_type VARCHAR(10) NOT NULL PRIMARY KEY,
    name VARCHAR(20)
);
INSERT INTO training_type (training_type, name) VALUES
    ('func',    'Кросcфит'),
    ('stretch', 'Растяжка'),
    ('yoga',    'Йога'),
    ('massage', 'Массаж');

ALTER SEQUENCE IF EXISTS user_info_user_id_seq
    RESTART 10000;

INSERT INTO user_info (user_id, first_name, last_name, nick_name, user_type) VALUES
    (1000, 'Ринат', 'Фаттяхудинов', 'Ринат', 'admin'),
    (1001, 'Нина', 'Елизова', 'Нина', 'trainer'),
    (1002, 'Ильза', 'Зырянова', 'Ильза', 'trainer');
INSERT INTO user_email (email, user_id, confirmed) VALUES
    ('rinchik_g@mail.ru', 1000, true);


CREATE TABLE IF NOT EXISTS trainer_type (
    user_id INTEGER NOT NULL REFERENCES user_info (user_id),
    training_type VARCHAR(10) NOT NULL REFERENCES training_type (training_type),
    PRIMARY KEY (user_id, training_type)
);

INSERT INTO trainer_type (user_id, training_type) VALUES
    (1000, 'func'),
    (1001, 'func'),
    (1001, 'stretch'),
    (1002, 'func'),
    (1002, 'stretch')
;

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

CREATE TABLE IF NOT EXISTS ticket (
    ticket_id SERIAL PRIMARY KEY ,
    ticket_type_id INTEGER REFERENCES ticket_type,
    user_id INTEGER REFERENCES user_info(user_id),
    ticket_start DATE
);

CREATE TABLE IF NOT EXISTS training_visit (
    training_id INTEGER NOT NULL REFERENCES training (training_id),
    user_id INTEGER NOT NULL REFERENCES user_info (user_id),
    visit_comment VARCHAR(10) NULL,  --
    ticket_id INTEGER NULL REFERENCES ticket(ticket_id)
);

