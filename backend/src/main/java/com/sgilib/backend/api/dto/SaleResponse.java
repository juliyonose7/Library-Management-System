package com.sgilib.backend.api.dto;

import java.time.OffsetDateTime;

public class SaleResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private Integer quantity;
    private OffsetDateTime soldAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public OffsetDateTime getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(OffsetDateTime soldAt) {
        this.soldAt = soldAt;
    }
}
