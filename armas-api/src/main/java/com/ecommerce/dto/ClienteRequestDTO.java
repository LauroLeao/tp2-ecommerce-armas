package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "Informe um e-mail valido")
        @Size(max = 160, message = "O e-mail deve ter no maximo 160 caracteres")
        String email,

        @NotBlank(message = "O telefone e obrigatorio")
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter 10 ou 11 digitos")
        String telefone,

        @NotBlank(message = "O CPF e obrigatorio")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve ter 11 digitos")
        String cpf,

        @Pattern(regexp = "(?:\\d{8})?", message = "O CEP deve ter 8 digitos quando informado")
        String cep,

        @NotBlank(message = "O logradouro e obrigatorio")
        @Size(max = 120, message = "O logradouro deve ter no maximo 120 caracteres")
        String logradouro,

        @NotBlank(message = "O numero e obrigatorio")
        @Size(max = 20, message = "O numero deve ter no maximo 20 caracteres")
        String numero,

        @Size(max = 120, message = "O complemento deve ter no maximo 120 caracteres")
        String complemento,

        @NotBlank(message = "O bairro e obrigatorio")
        @Size(max = 100, message = "O bairro deve ter no maximo 100 caracteres")
        String bairro,

        @NotBlank(message = "O municipio e obrigatorio")
        @Size(max = 100, message = "O municipio deve ter no maximo 100 caracteres")
        String municipioNome,

        @NotBlank(message = "O codigo IBGE do municipio e obrigatorio")
        @Pattern(regexp = "\\d{7}", message = "O codigo IBGE deve ter 7 digitos")
        String municipioCodigoIbge,

        @NotBlank(message = "A UF e obrigatoria")
        @Pattern(regexp = "[A-Z]{2}", message = "A UF deve ter 2 letras maiusculas")
        String estadoSigla,

        @NotBlank(message = "O estado e obrigatorio")
        @Size(max = 80, message = "O estado deve ter no maximo 80 caracteres")
        String estadoNome,

        String fotoBase64,
        boolean removerFoto) {
}
