package com.sgilib.backend.service.impl;

import com.sgilib.backend.api.dto.ClientRequest;
import com.sgilib.backend.domain.Client;
import com.sgilib.backend.exception.ConflictException;
import com.sgilib.backend.exception.ResourceNotFoundException;
import com.sgilib.backend.repository.ClientRepository;
import com.sgilib.backend.service.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> findAll(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id " + id));
    }

    @Override
    @Transactional
    public Client create(ClientRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (clientRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Client email already exists");
        }

        Client client = new Client();
        client.setFirstName(request.getFirstName().trim());
        client.setLastName(request.getLastName().trim());
        client.setEmail(email);
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public Client update(Long id, ClientRequest request) {
        Client client = findById(id);
        String email = request.getEmail().trim().toLowerCase();
        if (clientRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictException("Client email already exists");
        }

        client.setFirstName(request.getFirstName().trim());
        client.setLastName(request.getLastName().trim());
        client.setEmail(email);
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Client client = findById(id);
        clientRepository.delete(client);
    }
}
