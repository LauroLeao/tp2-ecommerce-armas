package com.ecommerce.dto;

import com.ecommerce.model.Modelo;

public record ModeloResponseDTO(Long id, String nome, Long marcaId, String marcaNome) {
    public static ModeloResponseDTO from(Modelo modelo) {
        return new ModeloResponseDTO(modelo.id, modelo.nome, modelo.marca.id, modelo.marca.nome);
    }
}
