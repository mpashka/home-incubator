ALTER SEQUENCE IF EXISTS user_info_user_id_seq
    RESTART 10000;

INSERT INTO user_type (user_type, name) VALUES
                                            ('guest',   'Гость'),
                                            ('user',    'Посетитель'),  -- Allow payments through app
                                            ('trainer', 'Тренер'),
                                            ('admin',   'Администратор');

INSERT INTO training_type (training_type, name) VALUES
                                                    ('func',    'Кросcфит'),
                                                    ('stretch', 'Растяжка'),
                                                    ('yoga',    'Йога'),
                                                    ('massage', 'Массаж');

INSERT INTO user_info (user_id, first_name, last_name, nick_name, user_type) VALUES
                                                                                 (1000, 'Ринат', 'Фаттяхудинов', 'Ринат', 'admin'),
                                                                                 (1001, 'Нина', 'Елизова', 'Нина', 'trainer'),
                                                                                 (1002, 'Ильза', 'Зырянова', 'Ильза', 'trainer');
INSERT INTO user_email (email, user_id, confirmed) VALUES
    ('rinchik_g@mail.ru', 1000, true);


INSERT INTO trainer_type (user_id, training_type) VALUES
                                                      (1000, 'func'),
                                                      (1001, 'func'),
                                                      (1001, 'stretch'),
                                                      (1002, 'func'),
                                                      (1002, 'stretch')
;

