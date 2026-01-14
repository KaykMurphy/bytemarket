package com.bytemarket.bytemarket_api.dto.response;

public record StockStatusDTO(
        Long productId,
        String productTitle,
        Long availableStock,
        Long soldStock,
        Long totalStock
) {}