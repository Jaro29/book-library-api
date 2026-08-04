CREATE TABLE IF NOT EXISTS books
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255)    NOT NULL,
    author      VARCHAR(255)    NOT NULL,
    isbn        VARCHAR(20)     UNIQUE,
    status      VARCHAR(20)     NOT NULL,
    start_date  DATE,
    finish_date DATE,
    times_read  INT             NOT NULL DEFAULT 0,
    notes       TEXT
);