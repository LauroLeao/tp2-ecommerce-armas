package com.ecommerce.dto;

import com.ecommerce.model.Cliente;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        String endereco) {

    public static ClienteResponseDTO from(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.id,
                cliente.nome,
                cliente.email,
                cliente.telefone,
                cliente.endereco);
    }
}
