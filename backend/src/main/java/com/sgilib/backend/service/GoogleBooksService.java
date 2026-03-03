package com.sgilib.backend.service;

import com.sgilib.backend.api.dto.GoogleBookMetadataResponse;

import java.util.Optional;

public interface GoogleBooksService {

    Optional<GoogleBookMetadataResponse> findByIsbn(String isbn);
}
