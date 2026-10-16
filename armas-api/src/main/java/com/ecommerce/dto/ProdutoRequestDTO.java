package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProdutoRequestDTO(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String nome,

        @Size(max = 1000, message = "A descricao deve ter no maximo 1000 caracteres")
        String descricao,

        @NotNull(message = "O preco e obrigatorio")
        @PositiveOrZero(message = "O preco nao pode ser negativo")
        Double preco,

        @NotNull(message = "O estoque e obrigatorio")
        @PositiveOrZero(message = "O estoque nao pode ser negativo")
        Integer estoque,

        @NotNull(message = "A categoria e obrigatoria")
        Long categoriaId,

        String imagemBase64,

        boolean removerImagem) {
}
