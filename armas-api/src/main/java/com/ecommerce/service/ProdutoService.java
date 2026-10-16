package com.ecommerce.service;

import com.ecommerce.dto.ProdutoRequestDTO;
import com.ecommerce.dto.ProdutoResponseDTO;
import com.ecommerce.dto.ProdutoImagemDTO;
import com.ecommerce.dto.PageResponse;

public interface ProdutoService {

    PageResponse<ProdutoResponseDTO> listar(String nome, int page, int pageSize);

    ProdutoResponseDTO buscarPorId(Long id);

    ProdutoImagemDTO buscarImagem(Long id);

    ProdutoResponseDTO cadastrar(ProdutoRequestDTO produto);

    ProdutoResponseDTO editar(Long id, ProdutoRequestDTO produto);

    void excluir(Long id);
}
