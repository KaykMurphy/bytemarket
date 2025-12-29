package com.bytemarket.bytemarket_api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderReceiptResponseDTO(
        Long id,
        Instant moment,
        BigDecimal total,
        String status,
        List<OrderItemReceiptResponseDTO> items
) {}
