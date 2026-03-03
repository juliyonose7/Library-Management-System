ALTER TABLE books
    ADD COLUMN subtitle VARCHAR(300),
    ADD COLUMN description TEXT,
    ADD COLUMN publisher VARCHAR(200),
    ADD COLUMN category VARCHAR(120),
    ADD COLUMN cover_url VARCHAR(500),
    ADD COLUMN page_count INTEGER;
