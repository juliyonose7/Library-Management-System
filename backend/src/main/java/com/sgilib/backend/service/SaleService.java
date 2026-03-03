package com.sgilib.backend.service;

import com.sgilib.backend.api.dto.SaleRequest;
import com.sgilib.backend.domain.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SaleService {

    Sale create(SaleRequest request);

    Page<Sale> findAll(Pageable pageable);

    Page<Sale> findByClientId(Long clientId, Pageable pageable);
}
