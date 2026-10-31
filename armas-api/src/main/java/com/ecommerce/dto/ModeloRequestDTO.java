package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModeloRequestDTO(
        @NotBlank(message = "O nome do modelo e obrigatorio")
        @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres")
        String nome,
        @NotNull(message = "Selecione uma marca")
        Long marcaId) {
}
