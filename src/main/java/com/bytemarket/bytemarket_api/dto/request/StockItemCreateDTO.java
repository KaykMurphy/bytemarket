package com.bytemarket.bytemarket_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StockItemCreateDTO(

        @NotBlank(message = "Conteúdo é obrigatório")
        @Pattern(
                regexp = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}:.+$",
                message = "Formato deve ser: email:senha"
        )
        String content // Formato: email:senha
) {}