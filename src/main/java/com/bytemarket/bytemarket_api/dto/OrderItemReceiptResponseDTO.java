package com.bytemarket.bytemarket_api.dto;

import com.bytemarket.bytemarket_api.domain.ProductType;
import java.math.BigDecimal;
import java.util.List;

public record OrderItemReceiptResponseDTO(
        String title,
        Integer quantity,
        BigDecimal price,
        List<String> deliveredContent //(Login/Senha ou Chave)
) {}
