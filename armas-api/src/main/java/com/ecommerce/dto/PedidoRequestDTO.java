package com.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PedidoRequestDTO(
        @NotNull(message = "Selecione um cliente") Long clienteId,
        @NotEmpty(message = "Adicione pelo menos um produto") List<@Valid ItemPedidoRequestDTO> itens) {
}
