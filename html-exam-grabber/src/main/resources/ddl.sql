
ALTER USER grabber WITH PASSWORD 'grabber';
ALTER ROLE grabber INHERIT;
GRANT ALL PRIVILEGES ON DATABASE grabber TO grabber;
--alter default privileges in schema public grant all PRIVILEGES ON TABLES to grabber;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO grabber;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO grabber;

DROP TABLE person_course;
DROP TABLE course;
DROP TABLE person_exams;
DROP TABLE exams;
DROP TABLE person;

CREATE TABLE IF NOT EXISTS person
(
    snils       varchar(11),
    name        varchar(100) NOT NULL,
    constraint snils_pk  PRIMARY KEY (snils)
);

CREATE TABLE IF NOT EXISTS exams
(
    exam_id     serial primary key ,
    exam_name   varchar(40),
    constraint unique_exam_name unique (exam_name)
);

CREATE TABLE IF NOT EXISTS person_exams
(
    snils       varchar(11),
    exam_id     integer not null references exams(exam_id),
    result      integer,
    constraint person_exams_pk unique (snils, exam_id)
);

CREATE TABLE IF NOT EXISTS course
(
    course_id       serial primary key ,
    institute_name  varchar(40),
    course_name     varchar(100),
    constraint unique_names unique (institute_name, course_name)
);

CREATE TABLE IF NOT EXISTS person_course
(
    snils       varchar(11) not null ,
    course_id   integer not null references course(course_id),
    agreement   boolean,
    original    boolean,
    score       integer,
    no_exam     boolean,
    -- ЕстьПреимущественноеПраво
    privilege_score   boolean,
    -- НаправляющаяОрганизация
    organization    varchar(40),
    -- Олимпиадник
    olympiad    boolean,
    -- Льготник
    privilege_total   boolean,
    -- Целевик
    targeted    boolean,

    constraint person_course_pk primary key (snils, course_id)
);

INSERT INTO exams (exam_name) VALUES
('история'), ('обществоведение'), ('русский');
