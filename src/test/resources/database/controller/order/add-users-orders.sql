INSERT INTO users (id, email, password, first_name, last_name)
VALUES (1, 'john@test.com', 'test', 'John', 'Doe');

INSERT INTO users (id, email, password, first_name, last_name)
VALUES (2, 'admin@test.com', 'test', 'Admin', 'Admin');

INSERT INTO roles (id, role_name)
VALUES (1, 'USER');

INSERT INTO roles (id, role_name)
VALUES (2, 'ADMIN');

INSERT INTO users_roles (user_id, role_id)
VALUES (1, 1);

INSERT INTO users_roles (user_id, role_id)
VALUES (2, 2);

INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted)
VALUES (1, 'Book 1', 'Author 1', 'ISBN-123456', 100, 'Description for Book 1', 'image1.jpg', FALSE);

INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted)
VALUES (2, 'Book 2', 'Author 2', 'ISBN-654321', 200, 'Description for Book 2', 'image2.jpg', FALSE);

INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted)
VALUES (3, 'Book 3', 'Author 3', 'ISBN-908765', 250, 'Description for Book 3', 'image3.jpg', FALSE);

INSERT INTO shopping_carts (id, user_id, is_deleted)
VALUES (1, 1, FALSE);

INSERT INTO cart_items (shopping_cart_id, book_id, quantity, is_deleted)
VALUES (1, 1, 1, FALSE);

INSERT INTO orders (id, user_id, status, total, order_date, shipping_address, is_deleted)
VALUES (1, 1, 'NEW', 600, '2023-09-13 00:31:58', 'Kyiv, NewPost110', FALSE);

INSERT INTO orders (id, user_id, status, total, order_date, shipping_address, is_deleted)
VALUES (2, 1, 'CANCELED', 450, '2023-09-12 00:31:58', 'Kyiv, NewPost110', FALSE);

INSERT INTO orders (id, user_id, status, total, order_date, shipping_address, is_deleted)
VALUES (3, 1, 'DELIVERED', 800, '2023-08-12 00:31:58', 'Kyiv, NewPost110', FALSE);

INSERT INTO order_items (id, order_id, book_id, quantity, price, is_deleted)
VALUES
    (1, 1, 1, 1, 50.00, false),
    (2, 2, 2, 1, 25.00, false),
    (3, 3, 3, 1, 75.00, false);
