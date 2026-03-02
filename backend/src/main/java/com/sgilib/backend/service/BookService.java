package com.sgilib.backend.service;

import com.sgilib.backend.api.dto.BookRequest;
import com.sgilib.backend.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    Page<Book> findAll(Pageable pageable);

    Book findById(Long id);

    Book create(BookRequest request);

    Book update(Long id, BookRequest request);

    void delete(Long id);
}
