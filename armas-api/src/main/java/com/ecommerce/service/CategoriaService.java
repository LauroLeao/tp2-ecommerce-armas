package com.ecommerce.service;

import com.ecommerce.dto.CategoriaRequestDTO;
import com.ecommerce.dto.CategoriaResponseDTO;
import java.util.List;

public interface CategoriaService {
    List<CategoriaResponseDTO> listar();

    CategoriaResponseDTO cadastrar(CategoriaRequestDTO dados);
}
