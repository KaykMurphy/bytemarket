package com.bytemarket.bytemarket_api.dto.request;

import com.bytemarket.bytemarket_api.domain.ProductType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductCreateDTO(

        @NotBlank(message = "Título é obrigatório")
        @Size(min = 3, max = 200, message = "Título deve ter entre 3 e 200 caracteres")
        String title,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(min = 10, max = 5000, message = "Descrição deve ter entre 10 e 5000 caracteres")
        String description,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço mínimo é R$ 0,01")
        BigDecimal price,

        @NotBlank(message = "URL da imagem é obrigatória")
        String imageUrl,

        @NotNull(message = "Tipo do produto é obrigatório")
        ProductType type
) {}