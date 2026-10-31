package com.ecommerce.service;

import com.ecommerce.dto.ClienteRequestDTO;
import com.ecommerce.dto.ClienteResponseDTO;
import com.ecommerce.dto.ProdutoImagemDTO;

import java.util.List;

public interface ClienteService {

    List<ClienteResponseDTO> listar();

    List<ClienteResponseDTO> sugerir(String nome);

    ClienteResponseDTO buscarPorId(Long id);

    ProdutoImagemDTO buscarFoto(Long id);

    ClienteResponseDTO cadastrar(ClienteRequestDTO cliente);

    ClienteResponseDTO editar(Long id, ClienteRequestDTO cliente);

    void excluir(Long id);
}
