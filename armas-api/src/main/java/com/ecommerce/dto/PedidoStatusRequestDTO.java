package com.ecommerce.dto;

import com.ecommerce.model.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record PedidoStatusRequestDTO(@NotNull StatusPedido status) {
}
