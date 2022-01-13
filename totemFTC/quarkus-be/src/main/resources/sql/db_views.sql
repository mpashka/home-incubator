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
