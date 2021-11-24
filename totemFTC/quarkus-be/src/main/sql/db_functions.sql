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
) RETURNS RECORD AS $$
DECLARE
    v_visit_cursor REFCURSOR;
    v_ticket_cursor REFCURSOR;
    v_visit training_visit%ROWTYPE;
    v_ticket ticket_and_type;
    v_training training%ROWTYPE;
    v_diff_visits INTEGER;
    v_update_ticket_id BOOLEAN;
BEGIN
    RAISE DEBUG 'debug mark_visit. training:%, user:%, self:%, master:%', p_training_id, p_user_id, p_mark_self, p_mark_master;

    SELECT t.* INTO STRICT v_training FROM training t WHERE t.training_id=p_training_id;

    OPEN v_visit_cursor NO SCROLL FOR
        SELECT t.*
        FROM training_visit t
        WHERE t.training_id=p_training_id
          AND t.user_id=p_user_id
          AND t.ticket_user_id=p_ticket_user_id;
    FETCH NEXT FROM v_visit_cursor INTO v_visit;

    RAISE DEBUG 'select visits found: %', FOUND;
    IF FOUND THEN
        v_diff_visits = mark_visit_calc_count(v_visit.visit_mark_self, v_visit.visit_mark_master)
            - mark_visit_calc_count(COALESCE(p_mark_self, v_visit.visit_mark_self), COALESCE(p_mark_master, v_visit.visit_mark_master));
        v_update_ticket_id = v_visit.ticket_id IS NULL;
        v_ticket_cursor = mark_visit_find_ticket(v_visit.ticket_id, p_ticket_user_id, v_diff_visits, v_training.training_type);
        FETCH NEXT FROM v_ticket_cursor INTO v_ticket;
        RAISE DEBUG 'ticket found: %', FOUND;
        IF NOT FOUND THEN
            v_update_ticket_id = TRUE;
            v_ticket_cursor = mark_visit_find_ticket(NULL, p_ticket_user_id, v_diff_visits, v_training.training_type);
            FETCH NEXT FROM v_ticket_cursor INTO v_ticket;
        END IF;
        CALL mark_visit_update_ticket(FOUND, v_diff_visits, v_ticket, v_ticket_cursor, v_training);
        IF v_update_ticket_id THEN
            UPDATE training_visit SET ticket_id=v_ticket.ticket_id WHERE CURRENT OF v_visit_cursor;
        END IF;
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
        v_diff_visits := mark_visit_calc_count(p_mark_self, p_mark_master);
        v_ticket_cursor := mark_visit_find_ticket(NULL, p_ticket_user_id, v_diff_visits, v_training.training_type);
        FETCH NEXT FROM v_ticket_cursor INTO v_ticket;
        RAISE DEBUG 'select ticket. Found: %', FOUND;
        IF FOUND THEN
            IF v_diff_visits IN (0,1) THEN
                CALL mark_visit_update_ticket(true, v_diff_visits, v_ticket, v_ticket_cursor, v_training);
            ELSE
                RAISE EXCEPTION 'Internal error. visits [%] NOT IN (0,1)', v_diff_visits USING HINT='Check mark_visit SQL function';
            END IF;
        END IF;

        INSERT INTO training_visit(training_id, user_id, ticket_user_id, ticket_id, visit_mark_schedule,
                                   visit_mark_self, visit_mark_master)
        VALUES (p_training_id, p_user_id, p_ticket_user_id, v_ticket.ticket_id, p_default_mark_schedule,
                p_mark_self, p_mark_master)
        RETURNING * INTO STRICT v_visit;
    END IF;

    RETURN v_ticket;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE mark_visit_update_ticket(p_found BOOLEAN, p_diff_visits INTEGER, p_ticket ticket_and_type/*record*/, p_ticket_cursor REFCURSOR,
                                                     p_training RECORD/*training%ROWTYPE*/) AS $$
DECLARE
    v_old_visits INTEGER;
    v_new_visits INTEGER;
BEGIN
    RAISE DEBUG 'update visit. ticket found: %', p_found;
    IF NOT p_found THEN
        RETURN;
    END IF;
    v_old_visits = p_ticket.training_visits;
    v_new_visits := v_old_visits + p_diff_visits;
    IF p_diff_visits<>0 THEN
        UPDATE training_ticket SET training_visits=v_new_visits WHERE CURRENT OF p_ticket_cursor;
        IF v_new_visits = 0 THEN
            UPDATE training_ticket SET ticket_start=NULL,ticket_end=NULL WHERE CURRENT OF p_ticket_cursor;
        ELSEIF v_new_visits > 0 AND p_ticket.ticket_start IS NULL THEN
            UPDATE training_ticket SET ticket_start=date_trunc('day', p_training.training_time) WHERE CURRENT OF p_ticket_cursor;
        ELSEIF v_new_visits < p_ticket.ticket_visits AND p_ticket.ticket_end IS NOT NULL THEN
            UPDATE training_ticket SET ticket_end=NULL WHERE CURRENT OF p_ticket_cursor;
        ELSEIF v_new_visits = p_ticket.ticket_visits THEN
            UPDATE training_ticket SET ticket_end=date_trunc('day', p_training.training_time) WHERE CURRENT OF p_ticket_cursor;
        END IF;
        p_ticket.training_visits=v_new_visits;
    END IF;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mark_visit_calc_count(p_mark_self mark_type_enum, p_mark_master mark_type_enum) RETURNS INTEGER AS $i$
BEGIN
    IF p_mark_master = 'on'::mark_type_enum THEN RETURN 1;
    ELSEIF p_mark_master = 'off'::mark_type_enum THEN RETURN 0;
    ELSEIF p_mark_self = 'on'::mark_type_enum THEN RETURN 1;
    ELSEIF p_mark_self = 'off'::mark_type_enum THEN RETURN 0;
    ELSE RETURN 0;
    END IF;
END
$i$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION mark_visit_find_ticket(p_ticket_id INTEGER, p_ticket_user_id INTEGER, p_visits_delta INTEGER,
                                                  p_training_type VARCHAR(10), OUT p_ticket_cursor REFCURSOR) AS $i$
DECLARE
    v_where VARCHAR;
    v_order_visit VARCHAR;
    v_order_date VARCHAR;
    v_tt VARCHAR(10)[];
BEGIN
    v_tt := ARRAY[p_training_type];
    RAISE DEBUG $$v_tt:%$$, v_tt;
    RAISE DEBUG $$find ticket. ticket:%, user:%, visits_delta:%, training_type:'%'$$, p_ticket_id, p_ticket_user_id, p_visits_delta, p_training_type;

    IF p_ticket_id IS NOT NULL THEN
        v_where := ' trt.ticket_id=$1 ';
    ELSE
        --v_where := $$ trt.user_id=$2 AND tty.ticket_training_types && $3 $$;
        --v_where := $$ trt.user_id=$2 AND tty.ticket_training_types && '{stretch}' $$;
        v_where := $$ trt.user_id=$2 $$;
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

    RAISE DEBUG USING MESSAGE=format(
            'SELECT *
             FROM training_ticket trt
                      JOIN ticket_type tty ON trt.ticket_type_id=tty.ticket_type_id
             WHERE %s
             ORDER BY trt.training_visits %s, trt.ticket_buy %s
             LIMIT 1
                 FOR UPDATE', v_where, v_order_visit, v_order_date);

    /*
    ticket_type_id SERIAL PRIMARY KEY ,
        ticket_training_types VARCHAR(10)[] NOT NULL,
    ticket_name VARCHAR(20)  NOT NULL,
    ticket_cost INTEGER NOT NULL,
    ticket_visits INTEGER NOT NULL,
    ticket_days INTEGER NOT NULL
     */

    OPEN p_ticket_cursor NO SCROLL FOR EXECUTE
        format(
                'SELECT trt.*,tty.ticket_training_types, tty.ticket_name, tty.ticket_cost, tty.ticket_visits, tty.ticket_days
                 FROM training_ticket trt
                          JOIN ticket_type tty ON trt.ticket_type_id=tty.ticket_type_id
                 WHERE %s
                 ORDER BY trt.training_visits %s, trt.ticket_buy %s
                 LIMIT 1
                     FOR UPDATE', v_where, v_order_visit, v_order_date)
        USING p_ticket_id, p_ticket_user_id, v_tt/*ARRAY[p_training_type]*/;
END
$i$ LANGUAGE plpgsql;
