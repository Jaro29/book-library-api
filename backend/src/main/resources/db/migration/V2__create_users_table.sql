CREATE TABLE IF NOT EXISTS users
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_name VARCHAR(50)  NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL
);