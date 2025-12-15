-- Этот файл позволяет выполнять SQL команды в тестовом и dev окружении
-- This file allows to write SQL commands that will be emitted in test and dev.

-- Вставка тестовых пользователей / Insert test users
INSERT INTO "user" (user_id, first_name, last_name) VALUES
(1, 'Иван', 'Петрович'),
(2, 'Мария', 'Иванова'),
(3, 'Петр', 'Сидоров');

-- Вставка магазинов / Insert shops
INSERT INTO shop (shop_id, name, tags) VALUES
(1, 'IDEA', ARRAY['продукты', 'супермаркет']),
(2, 'Maxi', ARRAY['продукты', 'гипермаркет']),
(3, 'Техномаркет', ARRAY['электроника', 'техника']),
(4, 'DM', ARRAY['косметика', 'здоровье']);

-- Вставка точек продаж / Insert shop points
INSERT INTO shop_point (shop_point_id, shop_id, tax_id, name, location_name, address, city, city_unit) VALUES
(1, 1, 123456789, 'IDEA Центр', 'ТЦ "Ушће"', 'Булевар Михаила Пупина 4', 'Београд', 'Нови Београд'),
(2, 1, 123456790, 'IDEA Звездара', 'ТЦ "Big"', 'Булевар Кнеза Александра Карађорђевића 126', 'Београд', 'Звездара'),
(3, 2, 234567890, 'Maxi Mall', 'ТЦ "Delta City"', 'Јурија Гагарина 16', 'Београд', 'Нови Београд'),
(4, 3, 345678901, 'Техномаркет Кнез Михаилова', 'Центар', 'Кнез Михаилова 54', 'Београд', 'Стари Град'),
(5, 4, 456789012, 'DM Теразије', 'Центар', 'Теразије 25', 'Београд', 'Стари Град');

-- Вставка категорий / Insert categories
INSERT INTO category (category_id, name) VALUES
(1, 'Продукты'),
(2, 'Молочные продукты'),
(3, 'Овощи и фрукты'),
(4, 'Мясо и рыба'),
(5, 'Электроника'),
(6, 'Бытовая техника'),
(7, 'Косметика'),
(8, 'Здоровье'),
(9, 'Напитки'),
(10, 'Хлеб и выпечка');

-- Вставка гарантий / Insert warranties
INSERT INTO warranty (warranty_id, purchase_date, period_months, status, notes) VALUES
(1, '2024-01-15 10:30:00', 24, 'активна', 'Гарантия на ноутбук'),
(2, '2024-06-20 14:15:00', 12, 'активна', 'Гарантия на смартфон'),
(3, '2023-03-10 09:00:00', 12, 'истекла', 'Гарантия на блендер');

-- Вставка чеков / Insert receipts
INSERT INTO receipt (receipt_id, user_id, shop_point_id, pos_time, total, image_path, created_at, tags) VALUES
(1, 1, 1, '2024-12-10 18:30:00', 2456.50, '/receipts/2024/12/receipt_001.jpg', '2024-12-10 18:35:00', ARRAY['продукты', 'неделя']),
(2, 1, 3, '2024-12-12 12:15:00', 850.00, NULL, '2024-12-12 12:20:00', ARRAY['продукты', 'быстро']),
(3, 2, 4, '2024-11-25 16:45:00', 45990.00, '/receipts/2024/11/receipt_003.jpg', '2024-11-25 17:00:00', ARRAY['электроника', 'подарок']),
(4, 1, 2, '2024-12-13 19:00:00', 1234.00, NULL, '2024-12-13 19:05:00', ARRAY['продукты']),
(5, 3, 5, '2024-12-14 10:30:00', 567.80, NULL, '2024-12-14 10:35:00', ARRAY['косметика']);

-- Вставка сырых данных чеков / Insert raw receipt data
INSERT INTO receipt_raw (receipt_raw_id, user_id, url, receipt_id) VALUES
(1, 1, 'https://suf.purs.gov.rs/v/?vl=...example1...', 1),
(2, 1, 'https://suf.purs.gov.rs/v/?vl=...example2...', 2),
(3, 2, 'https://suf.purs.gov.rs/v/?vl=...example3...', 3),
(4, 1, 'https://suf.purs.gov.rs/v/?vl=...example4...', NULL);

-- Вставка позиций покупок / Insert purchase items
INSERT INTO purchase_item (purchase_item_id, receipt_id, name, category_id, price, quantity, warranty_id, tags) VALUES
-- Чек 1: продукты
(1, 1, 'Молоко 3.2% 1л', 2, 125.00, 2, NULL, ARRAY['органика']),
(2, 1, 'Хлеб белый', 10, 89.00, 1, NULL, NULL),
(3, 1, 'Помидоры', 3, 199.50, 1, NULL, ARRAY['свежие']),
(4, 1, 'Говядина фарш 500г', 4, 450.00, 2, NULL, NULL),
(5, 1, 'Яблоки Гренни 1кг', 3, 180.00, 1, NULL, NULL),
(6, 1, 'Сок апельсиновый 1л', 9, 165.00, 3, NULL, NULL),

-- Чек 2: продукты быстро
(7, 2, 'Вода минеральная 1.5л', 9, 75.00, 4, NULL, NULL),
(8, 2, 'Шоколад молочный', 1, 220.00, 2, NULL, ARRAY['десерт']),
(9, 2, 'Круассан', 10, 95.00, 2, NULL, NULL),

-- Чек 3: электроника
(10, 3, 'Ноутбук Lenovo IdeaPad', 5, 45990.00, 1, 1, ARRAY['работа', 'новый']),

-- Чек 4: продукты
(11, 4, 'Сыр гауда 200г', 2, 289.00, 1, NULL, NULL),
(12, 4, 'Масло оливковое 500мл', 1, 345.00, 1, NULL, ARRAY['экстра']),
(13, 4, 'Паста спагетти 500г', 1, 120.00, 3, NULL, NULL),
(14, 4, 'Томатный соус 400г', 1, 95.00, 2, NULL, NULL),

-- Чек 5: косметика
(15, 5, 'Крем для лица', 7, 345.80, 1, NULL, ARRAY['уход']),
(16, 5, 'Шампунь 400мл', 7, 222.00, 1, NULL, NULL);

-- Обновление последовательностей / Update sequences
ALTER SEQUENCE user_user_id_seq RESTART WITH 4;
ALTER SEQUENCE shop_shop_id_seq RESTART WITH 5;
ALTER SEQUENCE shop_point_shop_point_id_seq RESTART WITH 6;
ALTER SEQUENCE category_category_id_seq RESTART WITH 11;
ALTER SEQUENCE warranty_warranty_id_seq RESTART WITH 4;
ALTER SEQUENCE receipt_receipt_id_seq RESTART WITH 6;
ALTER SEQUENCE receipt_raw_receipt_raw_id_seq RESTART WITH 5;
ALTER SEQUENCE purchase_item_purchase_item_id_seq RESTART WITH 17;