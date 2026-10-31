package com.ecommerce.dto;

import com.ecommerce.model.Fornecedor;

public record FornecedorResponseDTO(Long id, String nome, String email, String telefone,
        Long marcaId, String marcaNome) {
    public static FornecedorResponseDTO from(Fornecedor fornecedor) {
        return new FornecedorResponseDTO(fornecedor.id, fornecedor.nome, fornecedor.email,
                fornecedor.telefone, fornecedor.marca.id, fornecedor.marca.nome);
    }
}
