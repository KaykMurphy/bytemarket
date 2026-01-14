package com.bytemarket.bytemarket_api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderReceiptDTO(
        Long id,
        Instant moment,
        BigDecimal total,
        String status,
        List<OrderItemReceiptResponseDTO> items
) {}
