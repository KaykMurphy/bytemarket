package com.bytemarket.bytemarket_api.dto;

import java.util.List;
import java.util.UUID;

public record OrderRequestDTO(
        UUID userId,
        List<OrderItemDTO> items
) {
}
