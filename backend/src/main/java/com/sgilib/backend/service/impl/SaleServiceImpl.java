package com.sgilib.backend.service.impl;

import com.sgilib.backend.api.dto.SaleRequest;
import com.sgilib.backend.domain.Book;
import com.sgilib.backend.domain.Client;
import com.sgilib.backend.domain.Sale;
import com.sgilib.backend.exception.ConflictException;
import com.sgilib.backend.exception.ResourceNotFoundException;
import com.sgilib.backend.repository.BookRepository;
import com.sgilib.backend.repository.ClientRepository;
import com.sgilib.backend.repository.SaleRepository;
import com.sgilib.backend.service.SaleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;

    public SaleServiceImpl(SaleRepository saleRepository,
                           ClientRepository clientRepository,
                           BookRepository bookRepository) {
        this.saleRepository = saleRepository;
        this.clientRepository = clientRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional
    public Sale create(SaleRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id " + request.getClientId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + request.getBookId()));

        int quantity = request.getQuantity();
        if (book.getStock() < quantity) {
            throw new ConflictException("Not enough stock for this sale");
        }

        book.setStock(book.getStock() - quantity);
        bookRepository.save(book);

        Sale sale = new Sale();
        sale.setClient(client);
        sale.setBook(book);
        sale.setQuantity(quantity);
        sale.setSoldAt(OffsetDateTime.now());

        return saleRepository.save(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Sale> findAll(Pageable pageable) {
        return saleRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Sale> findByClientId(Long clientId, Pageable pageable) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with id " + clientId);
        }
        return saleRepository.findByClientIdOrderBySoldAtDesc(clientId, pageable);
    }
}
