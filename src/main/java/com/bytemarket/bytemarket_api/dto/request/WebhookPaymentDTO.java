package com.bytemarket.bytemarket_api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WebhookPaymentDTO(

        @JsonProperty("payment_id")
        String paymentId,

        @JsonProperty("external_id")
        String externalId,

        String status,

        @JsonProperty("paid_at")
        String paidAt,

        String event // "payment.approved", "payment.rejected", etc.
) {}