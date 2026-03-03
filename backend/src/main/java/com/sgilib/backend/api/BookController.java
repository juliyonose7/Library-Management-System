package com.sgilib.backend.api;

import com.sgilib.backend.api.dto.BookRequest;
import com.sgilib.backend.api.dto.BookResponse;
import com.sgilib.backend.api.dto.GoogleBookMetadataResponse;
import com.sgilib.backend.api.mapper.ApiMapper;
import com.sgilib.backend.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Page<BookResponse> findAll(Pageable pageable) {
        return bookService.findAll(pageable).map(ApiMapper::toBookResponse);
    }

    @GetMapping("/{id}")
    public BookResponse findById(@PathVariable Long id) {
        return ApiMapper.toBookResponse(bookService.findById(id));
    }

    @GetMapping("/enrich")
    public ResponseEntity<GoogleBookMetadataResponse> enrichByIsbn(@RequestParam String isbn) {
        return bookService.enrichByIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody BookRequest request) {
        return ApiMapper.toBookResponse(bookService.create(request));
    }

    @PutMapping("/{id}")
    public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ApiMapper.toBookResponse(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }
}
