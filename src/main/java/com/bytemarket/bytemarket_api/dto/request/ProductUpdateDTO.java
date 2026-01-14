package com.bytemarket.bytemarket_api.dto.request;

import com.bytemarket.bytemarket_api.domain.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateDTO(

        @Size(min = 3, max = 200, message = "Título deve ter entre 3 e 200 caracteres")
        String title,

        @Size(min = 10, max = 5000, message = "Descrição deve ter entre 10 e 5000 caracteres")
        String description,

        @DecimalMin(value = "0.01", message = "Preço mínimo é R$ 0,01")
        BigDecimal price,

        String imageUrl,

        ProductType type
) {}