package com.ecommerce.dto;

import com.ecommerce.model.ItemPedido;
import java.math.BigDecimal;

public record ItemPedidoResponseDTO(
        Long produtoId, String nomeProduto, Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
    public static ItemPedidoResponseDTO from(ItemPedido item) {
        return new ItemPedidoResponseDTO(item.produto.id, item.nomeProduto, item.quantidade,
                item.precoUnitario, item.precoUnitario.multiply(BigDecimal.valueOf(item.quantidade)));
    }
}
