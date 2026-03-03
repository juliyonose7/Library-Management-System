package com.sgilib.backend.api;

import com.sgilib.backend.api.dto.SaleRequest;
import com.sgilib.backend.api.dto.SaleResponse;
import com.sgilib.backend.api.mapper.ApiMapper;
import com.sgilib.backend.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(@Valid @RequestBody SaleRequest request) {
        return ApiMapper.toSaleResponse(saleService.create(request));
    }

    @GetMapping
    public Page<SaleResponse> findAll(Pageable pageable) {
        return saleService.findAll(pageable).map(ApiMapper::toSaleResponse);
    }

    @GetMapping("/client/{clientId}")
    public Page<SaleResponse> findByClient(@PathVariable Long clientId, Pageable pageable) {
        return saleService.findByClientId(clientId, pageable).map(ApiMapper::toSaleResponse);
    }
}
