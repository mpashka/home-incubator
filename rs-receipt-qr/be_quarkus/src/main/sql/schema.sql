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
CREATE TABLE receipt_raw (
    receipt_raw_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    receipt_id INTEGER REFERENCES receipt(receipt_id) ON DELETE SET NULL
);

-- Таблица позиций покупок / Purchase items table
CREATE TABLE purchase_item (
    purchase_item_id SERIAL PRIMARY KEY,
    receipt_id INTEGER NOT NULL REFERENCES receipt(receipt_id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    category_id INTEGER REFERENCES category(category_id) ON DELETE SET NULL,
    price NUMERIC(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    warranty_id INTEGER REFERENCES warranty(warranty_id) ON DELETE SET NULL,
    tags TEXT[]
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
CREATE INDEX idx_shop_point_shop_id ON shop_point(shop_id);
CREATE INDEX idx_purchase_item_receipt_id ON purchase_item(receipt_id);
CREATE INDEX idx_purchase_item_category_id ON purchase_item(category_id);
CREATE INDEX idx_purchase_item_warranty_id ON purchase_item(warranty_id);
