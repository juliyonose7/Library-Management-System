package com.sgilib.backend.service.impl;

import com.sgilib.backend.api.dto.BookRequest;
import com.sgilib.backend.api.dto.GoogleBookMetadataResponse;
import com.sgilib.backend.domain.Author;
import com.sgilib.backend.domain.Book;
import com.sgilib.backend.exception.ConflictException;
import com.sgilib.backend.exception.ResourceNotFoundException;
import com.sgilib.backend.repository.AuthorRepository;
import com.sgilib.backend.repository.BookRepository;
import com.sgilib.backend.service.BookService;
import com.sgilib.backend.service.GoogleBooksService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GoogleBooksService googleBooksService;

    public BookServiceImpl(BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           GoogleBooksService googleBooksService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.googleBooksService = googleBooksService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + id));
    }

    @Override
    @Transactional
    public Book create(BookRequest request) {
        String isbn = request.getIsbn().trim();
        if (bookRepository.existsByIsbn(isbn)) {
            throw new ConflictException("Book ISBN already exists");
        }

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + request.getAuthorId()));

        Book book = new Book();
        book.setTitle(request.getTitle().trim());
        book.setIsbn(isbn);
        book.setPublicationYear(request.getPublicationYear());
        book.setStock(request.getStock());
        book.setAuthor(author);
        applyMetadataFromGoogle(book, isbn);
        return bookRepository.save(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GoogleBookMetadataResponse> enrichByIsbn(String isbn) {
        return googleBooksService.findByIsbn(isbn);
    }

    @Override
    @Transactional
    public Book update(Long id, BookRequest request) {
        Book book = findById(id);
        String isbn = request.getIsbn().trim();
        if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new ConflictException("Book ISBN already exists");
        }

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + request.getAuthorId()));

        book.setTitle(request.getTitle().trim());
        book.setIsbn(isbn);
        book.setPublicationYear(request.getPublicationYear());
        book.setStock(request.getStock());
        book.setAuthor(author);
        applyMetadataFromGoogle(book, isbn);
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Book book = findById(id);
        bookRepository.delete(book);
    }

    private void applyMetadataFromGoogle(Book book, String isbn) {
        googleBooksService.findByIsbn(isbn).ifPresent(metadata -> {
            if (metadata.getSubtitle() != null) {
                book.setSubtitle(metadata.getSubtitle());
            }
            if (metadata.getDescription() != null) {
                book.setDescription(metadata.getDescription());
            }
            if (metadata.getPublisher() != null) {
                book.setPublisher(metadata.getPublisher());
            }
            if (metadata.getCategory() != null) {
                book.setCategory(metadata.getCategory());
            }
            if (metadata.getCoverUrl() != null) {
                book.setCoverUrl(metadata.getCoverUrl());
            }
            if (metadata.getPageCount() != null) {
                book.setPageCount(metadata.getPageCount());
            }
        });
    }
}
