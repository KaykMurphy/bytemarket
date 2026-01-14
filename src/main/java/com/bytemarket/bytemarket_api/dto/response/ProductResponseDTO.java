package com.bytemarket.bytemarket_api.dto.response;

import com.bytemarket.bytemarket_api.domain.ProductType;

import java.math.BigDecimal;

public record ProductResponseDTO(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String imageUrl,
        ProductType type,
        Long availableStock // Quantidade disponível para compra
) {}