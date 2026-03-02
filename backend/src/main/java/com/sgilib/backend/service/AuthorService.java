package com.sgilib.backend.service;

import com.sgilib.backend.api.dto.AuthorRequest;
import com.sgilib.backend.domain.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorService {

    Page<Author> findAll(Pageable pageable);

    Author findById(Long id);

    Author create(AuthorRequest request);

    Author update(Long id, AuthorRequest request);

    void delete(Long id);
}
