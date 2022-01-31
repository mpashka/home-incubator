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
