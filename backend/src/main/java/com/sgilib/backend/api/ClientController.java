package com.sgilib.backend.api;

import com.sgilib.backend.api.dto.ClientRequest;
import com.sgilib.backend.api.dto.ClientResponse;
import com.sgilib.backend.api.dto.SaleResponse;
import com.sgilib.backend.api.mapper.ApiMapper;
import com.sgilib.backend.service.ClientService;
import com.sgilib.backend.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;
    private final SaleService saleService;

    public ClientController(ClientService clientService, SaleService saleService) {
        this.clientService = clientService;
        this.saleService = saleService;
    }

    @GetMapping
    public Page<ClientResponse> findAll(Pageable pageable) {
        return clientService.findAll(pageable).map(ApiMapper::toClientResponse);
    }

    @GetMapping("/{id}")
    public ClientResponse findById(@PathVariable Long id) {
        return ApiMapper.toClientResponse(clientService.findById(id));
    }

    @GetMapping("/{id}/sales")
    public Page<SaleResponse> findSalesByClient(@PathVariable Long id, Pageable pageable) {
        return saleService.findByClientId(id, pageable).map(ApiMapper::toSaleResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create(@Valid @RequestBody ClientRequest request) {
        return ApiMapper.toClientResponse(clientService.create(request));
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ApiMapper.toClientResponse(clientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        clientService.delete(id);
    }
}
