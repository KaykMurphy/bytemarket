package com.bytemarket.bytemarket_api.dto;

import domain.ProductType;

import java.math.BigDecimal;

public record ProductResponseDTO(
        Long id,
        String title,
        BigDecimal price,
        String imageUrl,
        ProductType type
) {
}