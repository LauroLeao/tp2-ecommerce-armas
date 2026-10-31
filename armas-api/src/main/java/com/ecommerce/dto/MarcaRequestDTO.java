package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarcaRequestDTO(
        @NotBlank(message = "O nome da marca e obrigatorio")
        @Size(max = 80, message = "O nome deve ter no maximo 80 caracteres")
        String nome) {
}
