package com.bytemarket.bytemarket_api.dto;

import domain.ProductType;
import java.math.BigDecimal;

public record OrderItemReceiptResponseDTO(
        Long productId,
        String productName,
        ProductType type,
        Integer quantity,
        BigDecimal price,
        String credentialContent
) {
}
