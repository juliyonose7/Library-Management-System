package com.sgilib.backend.api.mapper;

import com.sgilib.backend.api.dto.AuthorResponse;
import com.sgilib.backend.api.dto.BookResponse;
import com.sgilib.backend.api.dto.ClientResponse;
import com.sgilib.backend.domain.Author;
import com.sgilib.backend.domain.Book;
import com.sgilib.backend.domain.Client;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static AuthorResponse toAuthorResponse(Author author) {
        AuthorResponse response = new AuthorResponse();
        response.setId(author.getId());
        response.setName(author.getName());
        response.setNationality(author.getNationality());
        return response;
    }

    public static ClientResponse toClientResponse(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setFirstName(client.getFirstName());
        response.setLastName(client.getLastName());
        response.setEmail(client.getEmail());
        return response;
    }

    public static BookResponse toBookResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setIsbn(book.getIsbn());
        response.setPublicationYear(book.getPublicationYear());
        response.setStock(book.getStock());
        response.setAuthorId(book.getAuthor().getId());
        response.setAuthorName(book.getAuthor().getName());
        return response;
    }
}
