package com.bytemarket.bytemarket_api.dto.response;

import java.math.BigDecimal;

public record OrderHistoryItemDTO(
        String productTitle,
        Integer quantity,
        BigDecimal price
) {}