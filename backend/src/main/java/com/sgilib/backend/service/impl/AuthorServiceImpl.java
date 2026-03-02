package com.sgilib.backend.service.impl;

import com.sgilib.backend.api.dto.AuthorRequest;
import com.sgilib.backend.domain.Author;
import com.sgilib.backend.exception.ConflictException;
import com.sgilib.backend.exception.ResourceNotFoundException;
import com.sgilib.backend.repository.AuthorRepository;
import com.sgilib.backend.repository.BookRepository;
import com.sgilib.backend.service.AuthorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Author> findAll(Pageable pageable) {
        return authorRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Author findById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + id));
    }

    @Override
    @Transactional
    public Author create(AuthorRequest request) {
        Author author = new Author();
        author.setName(request.getName().trim());
        author.setNationality(trimToNull(request.getNationality()));
        return authorRepository.save(author);
    }

    @Override
    @Transactional
    public Author update(Long id, AuthorRequest request) {
        Author author = findById(id);
        author.setName(request.getName().trim());
        author.setNationality(trimToNull(request.getNationality()));
        return authorRepository.save(author);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Author author = findById(id);
        if (bookRepository.existsByAuthorId(id)) {
            throw new ConflictException("Cannot delete author with associated books");
        }
        authorRepository.delete(author);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
