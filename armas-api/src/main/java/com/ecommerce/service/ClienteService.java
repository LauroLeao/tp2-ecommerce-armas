package com.ecommerce.service;

import com.ecommerce.dto.ClienteRequestDTO;
import com.ecommerce.dto.ClienteResponseDTO;

import java.util.List;

public interface ClienteService {

    List<ClienteResponseDTO> listar();

    ClienteResponseDTO buscarPorId(Long id);

    ClienteResponseDTO cadastrar(ClienteRequestDTO cliente);

    ClienteResponseDTO editar(Long id, ClienteRequestDTO cliente);

    void excluir(Long id);
}
