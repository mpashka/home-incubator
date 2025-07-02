-- Receipts table
CREATE TABLE receipt (
    id SERIAL PRIMARY KEY,
    shop_id INTEGER REFERENCES shop(id),
    date DATE NOT NULL,
    total NUMERIC(10,2) NOT NULL,
    image_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tags TEXT[]
);

-- 'tags' columns are used for filtering and editing tags in the web and mobile UI

-- Shops table
CREATE TABLE shop (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50)
);

-- Categories table
CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Purchase items table
CREATE TABLE purchase_item (
    id SERIAL PRIMARY KEY,
    receipt_id INTEGER REFERENCES receipt(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    category_id INTEGER REFERENCES category(id),
    price NUMERIC(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    warranty_id INTEGER REFERENCES warranty(id),
    tags TEXT[]
);

-- Warranty table
CREATE TABLE warranty (
    id SERIAL PRIMARY KEY,
    purchase_date DATE NOT NULL,
    period_months INTEGER NOT NULL,
    status VARCHAR(50) DEFAULT 'active',
    notes TEXT
); 