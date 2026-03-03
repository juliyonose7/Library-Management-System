package com.sgilib.backend.repository;

import com.sgilib.backend.domain.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Override
    @EntityGraph(attributePaths = {"client", "book", "book.author"})
    Page<Sale> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"client", "book", "book.author"})
    Page<Sale> findByClientIdOrderBySoldAtDesc(Long clientId, Pageable pageable);
}
