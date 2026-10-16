package com.ecommerce.dto;

import com.ecommerce.model.Categoria;

public record CategoriaResponseDTO(Long id, String nome) {

    public static CategoriaResponseDTO from(Categoria categoria) {
        return new CategoriaResponseDTO(categoria.id, categoria.nome);
    }
}
