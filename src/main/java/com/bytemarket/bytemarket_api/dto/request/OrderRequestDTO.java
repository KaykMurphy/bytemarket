package com.bytemarket.bytemarket_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderRequestDTO(

        @NotNull(message = "ID do usuário é obrigatório")
        UUID userId,

        @NotBlank(message = "Email de entrega é obrigatório")
        @Email(message = "Email de entrega inválido")
        String deliveryEmail,

        @NotEmpty(message = "Pedido deve conter ao menos 1 item")
        @Valid
        List<OrderItemDTO> items
) {}