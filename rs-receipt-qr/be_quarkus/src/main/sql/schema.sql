-- Таблица пользователей / Users table
CREATE TABLE "user" (
    user_id SERIAL PRIMARY KEY,
    first_name TEXT,
    last_name TEXT
);

-- Таблица магазинов / Shops table
CREATE TABLE shop (
    shop_id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    tags TEXT[]
);

-- Таблица точек продаж магазинов / Shop points table
CREATE TABLE shop_point (
    shop_point_id SERIAL PRIMARY KEY,
    shop_id INTEGER NOT NULL REFERENCES shop(shop_id) ON DELETE CASCADE,
    tax_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    location_name TEXT,
    address TEXT,
    city TEXT,
    city_unit TEXT
);

-- Таблица категорий / Categories table
CREATE TABLE category (
    category_id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

-- Таблица гарантий / Warranty table
CREATE TABLE warranty (
    warranty_id SERIAL PRIMARY KEY,
    purchase_date TIMESTAMP NOT NULL,
    period_months INTEGER NOT NULL,
    status TEXT DEFAULT 'active',
    notes TEXT
);

-- Таблица чеков / Receipts table
CREATE TABLE receipt (
    receipt_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    shop_point_id INTEGER REFERENCES shop_point(shop_point_id) ON DELETE SET NULL,
    pos_time TIMESTAMP NOT NULL,
    total NUMERIC(10,2) NOT NULL,
    image_path TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tags TEXT[]
);

-- Таблица сырых данных чеков (из QR кодов) / Raw receipt data table (from QR codes)
-- Используется для асинхронной обработки QR кодов на backend
CREATE TABLE receipt_raw (
    receipt_raw_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    receipt_id INTEGER REFERENCES receipt(receipt_id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status TEXT DEFAULT 'pending' -- pending, processing, completed, failed
);

-- Таблица позиций покупок / Receipt items table
CREATE TABLE receipt_item (
    receipt_item_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    receipt_id INTEGER NOT NULL REFERENCES receipt(receipt_id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    category_id INTEGER REFERENCES category(category_id) ON DELETE SET NULL,
    price NUMERIC(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    warranty_id INTEGER REFERENCES warranty(warranty_id) ON DELETE SET NULL,
    tags TEXT[]
);

-- Таблица данных чеков из OCR (текстовое сканирование) / Receipt text data table (from OCR)
-- Хранит PFR данные из сербских фискальных чеков
CREATE TABLE receipt_text (
    receipt_text_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    pfr_time TEXT NOT NULL,           -- ПФР време (e.g., "07.09.2024. 19:25:47")
    pfr_receipt_number TEXT NOT NULL, -- ПФР број рачуна (e.g., "P8BPS55R-P8BPS55R-71822")
    pfr_counter TEXT NOT NULL,        -- Бројач рачуна (e.g., "64763/71822ПП")
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status TEXT DEFAULT 'received'    -- received, processed, error
);

-- Комментарий: колонки 'tags' используются для фильтрации и редактирования тегов в веб и мобильном UI
-- Comment: 'tags' columns are used for filtering and editing tags in the web and mobile UI

-- Индексы для улучшения производительности / Indexes for performance improvement
CREATE INDEX idx_receipt_user_id ON receipt(user_id);
CREATE INDEX idx_receipt_shop_point_id ON receipt(shop_point_id);
CREATE INDEX idx_receipt_pos_time ON receipt(pos_time);
CREATE INDEX idx_receipt_created_at ON receipt(created_at);
CREATE INDEX idx_receipt_raw_user_id ON receipt_raw(user_id);
CREATE INDEX idx_receipt_raw_receipt_id ON receipt_raw(receipt_id);
CREATE INDEX idx_receipt_raw_status ON receipt_raw(status); -- Для быстрой выборки pending QR кодов
CREATE INDEX idx_shop_point_shop_id ON shop_point(shop_id);
CREATE INDEX idx_shop_point_tax_id ON shop_point(tax_id); -- Для поиска по tax_id при обработке QR
CREATE INDEX idx_receipt_item_user_id ON receipt_item(user_id);
CREATE INDEX idx_receipt_item_receipt_id ON receipt_item(receipt_id);
CREATE INDEX idx_receipt_item_category_id ON receipt_item(category_id);
CREATE INDEX idx_receipt_item_warranty_id ON receipt_item(warranty_id);
CREATE INDEX idx_receipt_text_user_id ON receipt_text(user_id);
CREATE INDEX idx_receipt_text_pfr_receipt_number ON receipt_text(pfr_receipt_number);
CREATE INDEX idx_receipt_text_created_at ON receipt_text(created_at);
