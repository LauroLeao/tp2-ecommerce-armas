package com.ecommerce.dto;

import com.ecommerce.model.Marca;

public record MarcaResponseDTO(Long id, String nome) {
    public static MarcaResponseDTO from(Marca marca) {
        return new MarcaResponseDTO(marca.id, marca.nome);
    }
}
