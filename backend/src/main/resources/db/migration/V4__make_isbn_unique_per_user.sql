ALTER TABLE books DROP CONSTRAINT isbn;
ALTER TABLE books ADD CONSTRAINT uq_books_user_isbn UNIQUE (user_id, isbn);