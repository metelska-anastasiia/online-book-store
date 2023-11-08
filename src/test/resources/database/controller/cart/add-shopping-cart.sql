INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted)
VALUES (1, 'Book 1', 'Author 1', 'ISBN-123456', 100, 'Description for Book 1', 'image1.jpg', FALSE);

INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted)
VALUES (2, 'Book 2', 'Author 2', 'ISBN-654321', 200, 'Description for Book 2', 'image2.jpg', FALSE);

INSERT INTO books (id, title, author, isbn, price, description, cover_image, is_deleted)
VALUES (3, 'Book 3', 'Author 3', 'ISBN-908765', 250, 'Description for Book 3', 'image3.jpg', FALSE);

INSERT INTO users (id, email, password, first_name, last_name)
VALUES (1, 'john@test.com', 'test', 'John', 'Doe');

INSERT INTO shopping_carts (id, user_id, is_deleted)
VALUES (1, 1, FALSE);

INSERT INTO cart_items (id, shopping_cart_id, book_id, quantity) VALUES (1, 1, 1, 5);
