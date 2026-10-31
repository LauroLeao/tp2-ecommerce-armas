package com.ecommerce.dto;

import com.ecommerce.model.Pedido;
import com.ecommerce.model.StatusPedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoResponseDTO(
        Long id, Long clienteId, String clienteNome, Instant criadoEm,
        StatusPedido status, BigDecimal total, List<ItemPedidoResponseDTO> itens) {
    public static PedidoResponseDTO from(Pedido pedido) {
        return new PedidoResponseDTO(pedido.id, pedido.cliente.id, pedido.cliente.nome,
                pedido.criadoEm, pedido.status, pedido.total,
                pedido.itens.stream().map(ItemPedidoResponseDTO::from).toList());
    }
}
