package com.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPedidoRequestDTO(
        @NotNull(message = "Selecione um produto") Long produtoId,
        @NotNull(message = "Informe a quantidade") @Positive(message = "A quantidade deve ser positiva") Integer quantidade) {
}
