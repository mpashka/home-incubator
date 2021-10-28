ALTER SEQUENCE IF EXISTS user_info_user_id_seq
    RESTART 10000;

INSERT INTO user_type_description(user_type, name) VALUES
                                            ('guest',   'Гость'),
                                            ('user',    'Посетитель'),  -- Allow payments through app
                                            ('trainer', 'Тренер'),
                                            ('admin',   'Администратор');

INSERT INTO training_type (training_type, name) VALUES
                                                    ('func',    'Кросcфит'),
                                                    ('stretch', 'Растяжка'),
                                                    ('yoga',    'Йога'),
                                                    ('massage', 'Массаж');

INSERT INTO user_info (user_id, first_name, last_name, nick_name, user_type, training_types) VALUES
                                                                                 (1000, 'Ринат', 'Фаттяхудинов', 'Ринат', 'admin', ARRAY['func']),
                                                                                 (1001, 'Нина', 'Елизова', 'Нина', 'trainer', ARRAY['func', 'stretch']),
                                                                                 (1002, 'Ильза', 'Зырянова', 'Ильза', 'trainer', ARRAY['func', 'stretch', 'yoga']);
INSERT INTO user_email (email, user_id, confirmed) VALUES
    ('rinchik_g@mail.ru', 1000, true);

INSERT INTO ticket_type (training_types,ticket_name,ticket_cost,visits, days) VALUES
    (ARRAY['func', 'stretch'], 'Групповые 1',    600,  1,   1),
    (ARRAY['func', 'stretch'], 'Групповые 8',   4200,  8,  65),
    (ARRAY['func', 'stretch'], 'Групповые 12',  6000, 12,  90),
    (ARRAY['func', 'stretch'], 'Групповые 16',  7200, 16, 200),
    (ARRAY['func', 'stretch'], 'Групповые 20',  9000, 20,  75),
    (ARRAY['func', 'stretch'], 'Групповые 24',  9000, 24,  60),
    (ARRAY['func', 'stretch'], 'Групповые 50', 22500, 50, 200)
;
