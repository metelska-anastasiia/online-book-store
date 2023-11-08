INSERT INTO users (id, email, password, first_name, last_name)
VALUES (1, 'john@test.com', '$2a$10$l7wlcrpU7ncwbVK/qMUafe7gSFrzpXz3xj4Y3tOJo7BgQZT4rxMaq', 'John', 'Doe');

INSERT INTO users (id, email, password, first_name, last_name)
VALUES (2, 'bob@test.com', 'testforbob', 'Bob', 'Davidson');

INSERT INTO roles (id, role_name)
VALUES (1, 'USER');

INSERT INTO roles (id, role_name)
VALUES (2, 'ADMIN');

INSERT INTO users_roles (user_id, role_id)
VALUES (1, 2);
INSERT INTO users_roles (user_id, role_id)
VALUES (2, 1);
