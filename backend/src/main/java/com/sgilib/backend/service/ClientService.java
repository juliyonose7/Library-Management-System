package com.sgilib.backend.service;

import com.sgilib.backend.api.dto.ClientRequest;
import com.sgilib.backend.domain.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    Page<Client> findAll(Pageable pageable);

    Client findById(Long id);

    Client create(ClientRequest request);

    Client update(Long id, ClientRequest request);

    void delete(Long id);
}
