package com.bytemarket.bytemarket_api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderHistoryDTO(
        Long orderId,
        Instant orderDate,
        BigDecimal total,
        String status,
        String deliveryEmail,
        List<OrderHistoryItemDTO> items
) {}