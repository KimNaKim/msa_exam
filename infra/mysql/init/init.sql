SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS product_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE product_db;
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) CHARACTER SET utf8mb4,
    price INT,
    stock_quantity INT
) CHARACTER SET utf8mb4;

INSERT INTO product (name, price, stock_quantity) VALUES ('노트북', 1500000, 10);
INSERT INTO product (name, price, stock_quantity) VALUES ('마우스', 30000, 50);
INSERT INTO product (name, price, stock_quantity) VALUES ('키보드', 80000, 20);

USE order_db;
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT,
    quantity INT,
    status VARCHAR(255) CHARACTER SET utf8mb4
) CHARACTER SET utf8mb4;

GRANT ALL PRIVILEGES ON product_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'root'@'%';
FLUSH PRIVILEGES;
