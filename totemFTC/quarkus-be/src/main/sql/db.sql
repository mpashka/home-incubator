CREATE DATABASE totemftc;
CREATE USER totemftc WITH ENCRYPTED PASSWORD 'totemftc';
GRANT ALL PRIVILEGES ON DATABASE totemftc TO totemftc;

-- Users
CREATE TYPE user_type_enum AS ENUM ('guest', 'user', 'trainer', 'admin');
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
    user_type user_type_enum NOT NULL DEFAULT 'guest',
    user_training_types VARCHAR(10)[]        -- this is for user_type=='trainer' or 'admin'
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
    ticket_type_id SERIAL PRIMARY KEY ,
    ticket_training_types VARCHAR(10)[] NOT NULL,
    ticket_name VARCHAR(20)  NOT NULL,
    ticket_cost INTEGER NOT NULL,
    ticket_visits INTEGER NOT NULL,
    ticket_days INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS training_ticket (
    ticket_id SERIAL PRIMARY KEY ,
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
    ticket_user_id INTEGER NULL REFERENCES user_info (user_id),
    visit_comment VARCHAR(200) NULL,  --
    ticket_id INTEGER NULL REFERENCES training_ticket(ticket_id),
    visit_mark_schedule BOOLEAN NOT NULL DEFAULT false,
    visit_mark_self mark_type_enum NOT NULL DEFAULT 'unmark'::mark_type_enum,
    visit_mark_master mark_type_enum NOT NULL DEFAULT 'unmark'::mark_type_enum,
    PRIMARY KEY (training_id, user_id, ticket_user_id)
);

CREATE TYPE ticket_and_type AS (
    ticket_id INTEGER,
    ticket_type_id INTEGER,
    user_id INTEGER,
    ticket_buy TIMESTAMP,
    ticket_start DATE,
    ticket_end DATE,
    training_visits INTEGER,

    ticket_training_types VARCHAR(10)[],
    ticket_name VARCHAR(20),
    ticket_cost INTEGER,
    ticket_visits INTEGER,
    ticket_days INTEGER
);

CREATE OR REPLACE FUNCTION mark_visit(p_training_id INTEGER, p_user_id INTEGER, p_ticket_user_id INTEGER,
                           p_mark_self mark_type_enum DEFAULT NULL,
                           p_mark_master mark_type_enum DEFAULT NULL,
                           p_default_mark_schedule BOOLEAN DEFAULT FALSE,
                           p_default_mark_self mark_type_enum DEFAULT NULL,
                           p_default_mark_master mark_type_enum DEFAULT NULL
) RETURNS ticket_and_type AS $$
<<main_block>>
DECLARE
    v_visit_cursor REFCURSOR;
    v_ticket_cursor REFCURSOR;
    v_visit training_visit%ROWTYPE;
    v_ticket ticket_and_type;
    v_ticket_id INTEGER;
    v_training training%ROWTYPE;
    v_diff_visits INTEGER;
BEGIN
    CREATE PROCEDURE update_visits(p_diff_visits INTEGER) AS $i$
    DECLARE
        v_old_visits INTEGER;
        v_new_visits INTEGER;
        v_ticket ticket_and_type := main_block.v_ticket;
        v_ticket_cursor REFCURSOR := main_block.v_ticket_cursor;
        v_training training%ROWTYPE := main_block.v_training;
    BEGIN
        IF NOT FOUND THEN
            RETURN;
        END IF;
        v_old_visits = v_ticket.training_visits;
        v_new_visits := v_old_visits + p_diff_visits;
        IF p_diff_visits<>0 THEN
            UPDATE training_ticket SET training_visits=v_new_visits WHERE CURRENT OF v_ticket_cursor;
            IF v_new_visits = 0 THEN
                UPDATE training_ticket SET ticket_start=NULL,ticket_end=NULL WHERE CURRENT OF v_ticket_cursor;
            ELSEIF v_new_visits > 0 AND v_ticket.ticket_start IS NULL THEN
                UPDATE training_ticket SET ticket_start=date_trunc('day', v_training.training_time) WHERE CURRENT OF v_ticket_cursor;
            ELSEIF v_new_visits < v_ticket.ticket_visits AND v_ticket.ticket_end IS NOT NULL THEN
                UPDATE training_ticket SET ticket_end=NULL WHERE CURRENT OF v_ticket_cursor;
            ELSEIF v_new_visits = v_ticket.ticket_visits THEN
                UPDATE training_ticket SET ticket_end=date_trunc('day', v_training.training_time) WHERE CURRENT OF v_ticket_cursor;
            END IF;
            v_ticket.training_visits=v_new_visits;
        END IF;
    END
    $i$ LANGUAGE plpgsql;

    CREATE FUNCTION calc_visits_count(p_mark_self mark_type_enum, p_mark_master mark_type_enum) RETURNS INTEGER AS $i$
    BEGIN
        IF p_mark_master = 'on'::mark_type_enum THEN RETURN 1;
        ELSEIF p_mark_master = 'off'::mark_type_enum THEN RETURN 0;
        ELSEIF p_mark_self = 'on'::mark_type_enum THEN RETURN 1;
        ELSEIF p_mark_self = 'off'::mark_type_enum THEN RETURN 0;
        ELSE RETURN 0;
        END IF;
    END
    $i$ LANGUAGE plpgsql;

    CREATE FUNCTION find_ticket(p_ticket_id INTEGER, p_ticket_user_id INTEGER, p_visits_delta INTEGER,
                                p_training_type VARCHAR(10), OUT p_ticket_cursor REFCURSOR) AS $i$
    DECLARE
        v_where VARCHAR;
        v_order_visit VARCHAR;
        v_order_date VARCHAR;
    BEGIN
        IF p_ticket_id IS NOT NULL THEN
            v_where = ' trt.ticket_id=$1 ';
        ELSE
            v_where = ' trt.user_id=$2 AND $3=ANY(tty.ticket_training_types) ';
        END IF;

        IF p_visits_delta >= 0 THEN
            IF p_ticket_id IS NOT NULL AND p_visits_delta=0 THEN
                v_where := v_where || ' AND trt.training_visits <= tty.ticket_visits ';
            ELSE
                v_where := v_where || ' AND trt.training_visits < tty.ticket_visits ';
            END IF;
            v_order_visit := 'DESC';
            v_order_date := 'ASC';
        ELSE
            v_where := v_where || ' AND trt.training_visits > 0 ';
            v_order_visit := 'ASC';
            v_order_date := 'DESC';
        END IF;

        OPEN p_ticket_cursor NO SCROLL FOR EXECUTE
            format(
               'SELECT *
                FROM training_ticket trt
                         JOIN ticket_type tty ON trt.ticket_type_id=tty.ticket_type_id
                WHERE %s
                ORDER BY trt.training_visits %s, trt.ticket_buy %s
                LIMIT 1
                    FOR UPDATE', v_where, v_order_visit, v_order_date)
            USING p_ticket_id, p_ticket_user_id, p_training_type;
    END
    $i$ LANGUAGE plpgsql;


    SELECT t.* INTO STRICT v_training FROM training t WHERE t.training_id=p_training_id;

    OPEN v_visit_cursor NO SCROLL FOR
        SELECT t.*
        FROM training_visit t
        WHERE t.training_id=p_training_id
          AND t.user_id=p_user_id
          AND t.ticket_user_id=p_ticket_user_id;
    FETCH NEXT FROM v_visit_cursor INTO v_visit;

    IF FOUND THEN
        v_diff_visits = calc_visits_count(v_visit.visit_mark_self, v_visit.visit_mark_master)
            - calc_visits_count(COALESCE(p_mark_self, v_visit.visit_mark_self), COALESCE(p_mark_master, v_visit.visit_mark_master));
        v_ticket_cursor = find_ticket(v_visit.ticket_id, p_ticket_user_id, v_diff_visits, v_training.training_type);
        FETCH NEXT FROM v_ticket_cursor INTO v_ticket;
        IF NOT FOUND THEN
            v_ticket_cursor = find_ticket(NULL, p_ticket_user_id, v_diff_visits, v_training.training_type);
            FETCH NEXT FROM v_ticket_cursor INTO v_ticket;
        END IF;
        CALL update_visits(v_diff_visits);
        IF p_mark_self IS NOT NULL THEN
            UPDATE training_visit SET visit_mark_self=p_mark_self WHERE CURRENT OF v_visit_cursor;
        ELSEIF p_mark_master IS NOT NULL THEN
            UPDATE training_visit SET visit_mark_master=p_mark_master WHERE CURRENT OF v_visit_cursor;
        ELSE
            UPDATE training_visit SET visit_mark_schedule=p_default_mark_schedule WHERE CURRENT OF v_visit_cursor;
        END IF;
    ELSE
        p_mark_self := COALESCE(p_mark_self, p_default_mark_self);
        p_mark_master := COALESCE(p_mark_master, p_default_mark_master);
        v_diff_visits := calc_visits_count(p_mark_self, p_mark_master);
        v_ticket_cursor := find_ticket(NULL, p_ticket_user_id, v_diff_visits, v_training.training_type);
        FETCH NEXT FROM v_ticket_cursor INTO v_ticket;
        IF FOUND THEN
            v_ticket_id = v_ticket.ticket_id;
            IF v_diff_visits=0 THEN
                NULL;
            ELSEIF v_diff_visits=1 THEN
                CALL update_visits(v_diff_visits);
            ELSE
                RAISE EXCEPTION 'Internal error. visits [%] NOT IN (0,1)', v_diff_visits USING HINT='Check mark_visit SQL function';
            END IF;
        END IF;

        INSERT INTO training_visit(training_id, user_id, ticket_user_id, ticket_id, visit_mark_schedule,
                                   visit_mark_self, visit_mark_master)
            VALUES (p_training_id, p_user_id, p_ticket_user_id, v_ticket_id, p_default_mark_schedule,
                    p_mark_self, p_mark_master)
            RETURNING * INTO STRICT v_visit;
    END IF;

    RETURN v_ticket;
END;
$$ LANGUAGE plpgsql;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO totemftc;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO totemftc;
