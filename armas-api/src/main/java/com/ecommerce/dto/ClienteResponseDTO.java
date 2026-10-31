package com.ecommerce.dto;

import com.ecommerce.model.Cliente;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        String endereco,
        String cpf,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String municipioNome,
        String municipioCodigoIbge,
        String estadoSigla,
        String estadoNome,
        String fotoUrl) {

    public static ClienteResponseDTO from(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.id,
                cliente.nome,
                cliente.email,
                cliente.telefone,
                cliente.endereco,
                cliente.cpf,
                cliente.cep,
                cliente.logradouro,
                cliente.numero,
                cliente.complemento,
                cliente.bairro,
                cliente.municipio != null ? cliente.municipio.nome : null,
                cliente.municipio != null ? cliente.municipio.codigoIbge : null,
                cliente.municipio != null ? cliente.municipio.estado.sigla : null,
                cliente.municipio != null ? cliente.municipio.estado.nome : null,
                cliente.foto != null ? "/clientes/" + cliente.id + "/foto?v=" + cliente.foto.id : null);
    }
}
