package com.bytemarket.bytemarket_api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PixPaymentResponseDTO(
        Long paymentId,
        String externalId,
        BigDecimal amount,
        String status,
        String pixQrCode,      // Base64 da imagem ou URL
        String pixQrCodeText,  // Código copia-cola
        Instant expiresAt,
        Long orderId
) {}