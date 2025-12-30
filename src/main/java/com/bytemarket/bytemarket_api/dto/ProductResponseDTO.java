package com.bytemarket.bytemarket_api.dto;

import com.bytemarket.bytemarket_api.domain.ProductType;

import java.math.BigDecimal;

public record ProductResponseDTO(
        Long id,
        String title,
        BigDecimal price,
        String imageUrl,
        ProductType type
) {
}