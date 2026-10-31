package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FornecedorRequestDTO(
        @NotBlank(message = "O nome do fornecedor e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String nome,
        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "Informe um e-mail valido")
        @Size(max = 160, message = "O e-mail deve ter no maximo 160 caracteres")
        String email,
        @NotBlank(message = "O telefone e obrigatorio")
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter 10 ou 11 digitos")
        String telefone,
        @NotNull(message = "Selecione uma marca")
        Long marcaId) {
}
