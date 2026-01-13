package com.bytemarket.bytemarket_api.dto;

import com.bytemarket.bytemarket_api.domain.Role;

public record RegisterRequestDTO(String name, String email, String password, Role role) {
}
