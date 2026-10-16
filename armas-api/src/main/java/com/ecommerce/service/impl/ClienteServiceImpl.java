package com.ecommerce.service.impl;

import com.ecommerce.dto.ClienteRequestDTO;
import com.ecommerce.dto.ClienteResponseDTO;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Cliente;
import com.ecommerce.repository.ClienteRepository;
import com.ecommerce.service.ClienteService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;

    public ClienteServiceImpl(ClienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ClienteResponseDTO> listar() {
        return repository.listAll().stream()
                .map(ClienteResponseDTO::from)
                .toList();
    }

    @Override
    public ClienteResponseDTO buscarPorId(Long id) {
        return ClienteResponseDTO.from(encontrarCliente(id));
    }

    @Override
    @Transactional
    public ClienteResponseDTO cadastrar(ClienteRequestDTO dados) {
        Cliente cliente = new Cliente();
        preencher(cliente, dados);
        repository.persist(cliente);
        return ClienteResponseDTO.from(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO editar(Long id, ClienteRequestDTO dados) {
        Cliente cliente = encontrarCliente(id);
        preencher(cliente, dados);
        return ClienteResponseDTO.from(cliente);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Cliente cliente = encontrarCliente(id);
        repository.delete(cliente);
    }

    private Cliente encontrarCliente(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado"));
    }

    private void preencher(Cliente cliente, ClienteRequestDTO dados) {
        cliente.nome = dados.nome();
        cliente.email = dados.email();
        cliente.telefone = dados.telefone();
        cliente.endereco = dados.endereco();
    }
}
