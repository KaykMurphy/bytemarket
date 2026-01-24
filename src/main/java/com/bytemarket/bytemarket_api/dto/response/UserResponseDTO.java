package com.bytemarket.bytemarket_api.dto.response;

import com.bytemarket.bytemarket_api.domain.Role;

import java.util.UUID;

public record UserResponseDTO (
        UUID id,
        String name,
        String email,
        Role role
){
}
