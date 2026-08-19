-- V1__init_schema.sql
-- Product Service schema: categories, products, product_images.

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    slug        VARCHAR(150) UNIQUE,
    parent_id   BIGINT REFERENCES categories (id) ON DELETE SET NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku         VARCHAR(50)   NOT NULL UNIQUE,
    name        VARCHAR(200)  NOT NULL,
    description VARCHAR(2000),
    price       NUMERIC(12,2) NOT NULL CHECK (price > 0),
    category_id BIGINT        NOT NULL REFERENCES categories (id),
    brand       VARCHAR(100),
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_name ON products (name);
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_active ON products (active);

CREATE TABLE product_images (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id    UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    url           VARCHAR(1024) NOT NULL,
    display_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_product_images_product_id ON product_images (product_id);

-- Seed a starter category tree so the catalog isn't empty on first boot.
INSERT INTO categories (name, description, slug) VALUES
    ('Electronics', 'Phones, laptops, and other electronic devices', 'electronics'),
    ('Fashion', 'Clothing, footwear, and accessories', 'fashion'),
    ('Home & Kitchen', 'Furniture, appliances, and kitchenware', 'home-kitchen'),
    ('Books', 'Physical and digital books', 'books');
