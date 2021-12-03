/*
-- Already present in db_data

COPY public.ticket_type (ticket_type_id, ticket_name, ticket_cost, ticket_visits, ticket_training_types, ticket_days) FROM stdin;
1	Групповые 1	600	1	{func,stretch}	1
2	Групповые 8	4200	8	{func,stretch}	65
3	Групповые 12	6000	12	{func,stretch}	90
4	Групповые 16	7200	16	{func,stretch}	200
5	Групповые 20	9000	20	{func,stretch}	75
6	Групповые 24	9000	24	{func,stretch}	60
7	Групповые 50	22500	50	{func,stretch}	200
\.

COPY public.training_type (training_type, name) FROM stdin;
func	Кросcфит
stretch	Растяжка
yoga	Йога
massage	Массаж
\.

COPY public.user_type_description (user_type, name) FROM stdin;
guest	Гость
user	Посетитель
trainer	Тренер
admin	Администратор
\.

*/

COPY public.training_schedule (training_schedule_id, training_time, trainer_id, training_type) FROM stdin;
3	1970-01-01 12:30:00	1001	stretch
4	1970-01-01 23:04:00	1000	func
1	1970-01-01 22:57:00	1000	func
5	1970-01-01 10:00:00	1001	yoga
6	1970-01-02 23:06:00	1000	func
7	1970-01-01 01:00:00	1000	massage
8	1970-01-01 22:55:00	1001	stretch
9	1970-01-02 08:35:00	1000	func
10	1970-01-02 10:00:00	1001	stretch
11	1970-01-03 08:35:00	1001	stretch
12	1970-01-04 08:35:00	1000	func
13	1970-01-05 08:35:00	1000	func
14	1970-01-06 08:35:00	1001	stretch
15	1970-01-07 08:35:00	1000	func
\.

COPY public.training (training_id, training_schedule_id, training_time, trainer_id, training_type, training_comment) FROM stdin;
20	3	2021-10-11 12:30:00	1001	stretch	\N
21	4	2021-10-11 23:04:00	1000	func	\N
22	1	2021-10-11 22:57:00	1000	func	\N
23	5	2021-10-11 10:00:00	1001	yoga	\N
24	6	2021-10-12 23:06:00	1000	func	\N
25	7	2021-10-11 01:00:00	1000	massage	\N
26	8	2021-10-11 22:55:00	1001	stretch	\N
27	3	2021-10-14 12:30:00	1001	stretch	\N
28	4	2021-10-14 23:04:00	1000	func	\N
29	1	2021-10-14 22:57:00	1000	func	\N
30	5	2021-10-14 10:00:00	1001	yoga	\N
31	6	2021-10-15 23:06:00	1000	func	\N
32	7	2021-10-14 01:00:00	1000	massage	\N
33	8	2021-10-14 22:55:00	1001	stretch	\N
34	9	2021-10-15 08:35:00	1000	func	\N
35	10	2021-10-15 10:00:00	1001	stretch	\N
36	11	2021-10-16 08:35:00	1001	stretch	\N
37	12	2021-10-17 08:35:00	1000	func	\N
38	13	2021-10-18 08:35:00	1000	func	\N
39	15	2021-10-13 08:35:00	1000	func	\N
40	11	2021-11-10 08:35:00	1001	stretch	\N
54	12	2021-11-11 08:35:00	1000	func	\N
55	13	2021-11-12 08:35:00	1000	func	\N
56	14	2021-11-13 08:35:00	1001	stretch	\N
57	15	2021-11-14 08:35:00	1000	func	\N
58	3	2021-11-15 12:30:00	1001	stretch	\N
59	4	2021-11-15 23:04:00	1000	func	\N
60	1	2021-11-15 22:57:00	1000	func	\N
61	5	2021-11-15 10:00:00	1001	yoga	\N
62	7	2021-11-15 01:00:00	1000	massage	\N
63	8	2021-11-15 22:55:00	1001	stretch	\N
64	6	2021-11-16 23:06:00	1000	func	\N
65	9	2021-11-16 08:35:00	1000	func	\N
66	10	2021-11-16 10:00:00	1001	stretch	\N
\.

COPY public.user_info (user_id, first_name, last_name, nick_name, primary_image, user_type, user_training_types) FROM stdin;
3	a	b	c	\N	guest	\N
10001	Veritatem	Quaeres	\N	5	guest	\N
\.

COPY public.user_email (email, user_id, confirmed) FROM stdin;
v.q007@ya.ru	10001	t
\.

COPY public.user_social_network (network_name, id, user_id, link, display_name) FROM stdin;
yandex	agent007	1000	\N	\N
\.

COPY public.user_session (session_id, user_id, last_update) FROM stdin;
session_10001	10001	\N	\N
\.

COPY public.training_ticket (ticket_id, ticket_type_id, user_id, ticket_start, ticket_end, ticket_buy, training_visits) FROM stdin;
3	3	10001	\N	\N	2021-11-10 16:38:12.025047	0
4	2	10001	\N	\N	2021-11-10 16:38:12.025047	0
\.
