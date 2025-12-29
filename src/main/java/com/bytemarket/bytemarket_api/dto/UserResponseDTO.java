package com.bytemarket.bytemarket_api.dto;

import domain.Order;

import java.util.List;
import java.util.UUID;

public record UserResponseDTO (
        UUID id,
        String name,
        String email
){
}
