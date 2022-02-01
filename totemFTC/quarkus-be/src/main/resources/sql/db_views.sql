CREATE OR REPLACE VIEW user_info_full (
    user_id,
    first_name,
    last_name,
    nick_name,
    primary_image,
    user_types,
    user_training_types,
    social_networks,
    emails,
    phones,
    images
) AS SELECT u.user_id,
            u.first_name,
            u.last_name,
            u.nick_name,
            u.primary_image,
            u.user_types,
            u.user_training_types,
    sn.social_networks,
    e.emails,
    p.phones,
    i.images
FROM user_info u
    LEFT JOIN LATERAL (SELECT array_agg(row_to_json(sn.*)) AS social_networks FROM user_social_network sn WHERE sn.user_id=u.user_id) sn ON true
    LEFT JOIN LATERAL (SELECT array_agg(row_to_json(e.*)) AS emails FROM user_email e WHERE e.user_id=u.user_id) e ON true
    LEFT JOIN LATERAL (SELECT array_agg(row_to_json(p.*)) AS phones FROM user_phone p WHERE p.user_id=u.user_id) p ON true
    LEFT JOIN LATERAL (SELECT array_agg(json_build_object('image_id', i.image_id, 'content_type', i.content_type)) AS images FROM user_image i WHERE i.user_id=u.user_id) i ON true
;

CREATE OR REPLACE VIEW ticket_type_full (
    ticket_type_id,
    ticket_training_types,
    ticket_name,
    ticket_cost,
    ticket_visits,
    ticket_days,
    ticket_training_types_obj
) AS SELECT
    ticket_type.ticket_type_id,
    ticket_type.ticket_training_types,
    ticket_type.ticket_name,
    ticket_type.ticket_cost,
    ticket_type.ticket_visits,
    ticket_type.ticket_days,
    ticket_training_type.ticket_training_types_obj
FROM ticket_type
    LEFT JOIN LATERAL (
        SELECT array_agg(to_jsonb(training_type.*)) AS ticket_training_types_obj
        FROM training_type
        WHERE training_type.training_type=ANY(ticket_type.ticket_training_types)
    ) ticket_training_type ON true
;

CREATE OR REPLACE VIEW ticket_view (
    -- training_ticket
    ticket_id,
    ticket_type_id,
    user_id,
    ticket_buy,
    ticket_start,
    ticket_end,
    training_visits,
    -- ticket_type
    ticket_training_types,
    ticket_name,
    ticket_cost,
    ticket_visits,
    ticket_days,
    -- ticket_type.ticket_training_types + training_type
    ticket_training_types_obj
) AS SELECT
    training_ticket.ticket_id,
    training_ticket.ticket_type_id,
    training_ticket.user_id,
    training_ticket.ticket_buy,
    training_ticket.ticket_start,
    training_ticket.ticket_end,
    training_ticket.training_visits,
    ticket_type.ticket_training_types,
    ticket_type.ticket_name,
    ticket_type.ticket_cost,
    ticket_type.ticket_visits,
    ticket_type.ticket_days,
    ticket_training_type.ticket_training_types_obj
FROM training_ticket
    JOIN ticket_type USING (ticket_type_id)
    LEFT JOIN LATERAL (
        SELECT array_agg(to_jsonb(training_type.*)) AS ticket_training_types_obj
        FROM training_type
        WHERE training_type.training_type=ANY(ticket_type.ticket_training_types)
    ) ticket_training_type ON true
;

CREATE OR REPLACE VIEW training_view (
    training_id,
    training_schedule_id,
    training_time,
    trainer_id,
    training_type,
    training_comment,
    trainer,
    training_name,
    default_cost
) AS SELECT
    training.training_id,
    training.training_schedule_id,
    training.training_time,
    training.trainer_id,
    training.training_type,
    training.training_comment,
    row_to_json(trainer.*) AS trainer,
    training_type.training_name,
    training_type.default_cost
FROM training
    LEFT JOIN user_info AS trainer ON trainer.user_id=training.trainer_id
    LEFT JOIN training_type USING (training_type)
;

-- todo it is possible to optimize this view and split into several different views with ticket, trainer, user
CREATE OR REPLACE VIEW visit_view (
    -- training_visit
    training_id,
    user_id,
    ticket_user_id,
    ticket_id,
    visit_mark_schedule,
    visit_mark_self,
    visit_mark_master,
    visit_comment,

    -- training
    training_schedule_id,
    training_time,
    trainer_id,
    training_type,
    training_comment,

    -- training_type
    training_name,
    default_cost,

    user_info,
    -- user_info for sorting
    user_first_name,
    user_last_name,
    user_nick_name,
    trainer,

    -- training_ticket
    ticket_type_id,
    ticket_buy,
    ticket_start,
    ticket_end,
    training_visits,

    -- ticket_type
    ticket_training_types,
    ticket_name,
    ticket_cost,
    ticket_visits,
    ticket_days

) AS SELECT
    training_visit.training_id,
    training_visit.user_id,
    training_visit.ticket_user_id,
    training_visit.ticket_id,
    training_visit.visit_mark_schedule,
    training_visit.visit_mark_self,
    training_visit.visit_mark_master,
    training_visit.visit_comment,

    training.training_schedule_id,
    training.training_time,
    training.trainer_id,
    training.training_type,
    training.training_comment,

    training_type.training_name,
    training_type.default_cost,

    row_to_json(user_info.*) AS user_info,
    user_info.first_name,
    user_info.last_name,
    user_info.nick_name,
    row_to_json(trainer.*) AS trainer,

    training_ticket.ticket_type_id,
    training_ticket.ticket_buy,
    training_ticket.ticket_start,
    training_ticket.ticket_end,
    training_ticket.training_visits,

    ticket_type.ticket_training_types,
    ticket_type.ticket_name,
    ticket_type.ticket_cost,
    ticket_type.ticket_visits,
    ticket_type.ticket_days
FROM training_visit
    LEFT JOIN training USING(training_id)
    LEFT JOIN training_type USING(training_type)
    LEFT JOIN user_info USING(user_id)
    LEFT JOIN user_info AS trainer ON trainer.user_id=training.trainer_id
    LEFT OUTER JOIN training_ticket USING(ticket_id)
    LEFT OUTER JOIN ticket_type USING(ticket_type_id)
;
