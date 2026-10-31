package com.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroContaDTO(
        @Valid ClienteRequestDTO cliente,
        @NotBlank @Size(min = 8, max = 72) String senha) {
}
