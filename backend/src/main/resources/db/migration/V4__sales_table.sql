CREATE TABLE sales (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    sold_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_sales_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_sales_book FOREIGN KEY (book_id) REFERENCES books (id)
);

CREATE INDEX idx_sales_client_id ON sales(client_id);
CREATE INDEX idx_sales_book_id ON sales(book_id);
CREATE INDEX idx_sales_sold_at ON sales(sold_at);
