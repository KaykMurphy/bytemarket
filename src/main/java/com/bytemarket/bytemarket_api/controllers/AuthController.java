package com.bytemarket.bytemarket_api.controllers;

import com.bytemarket.bytemarket_api.dto.request.LoginRequestDTO;
import com.bytemarket.bytemarket_api.dto.response.LoginResponseDTO;
import com.bytemarket.bytemarket_api.dto.request.RegisterRequestDTO;
import com.bytemarket.bytemarket_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO body) {
        LoginResponseDTO token = authService.login(body);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO body) {
        authService.register(body);
        return ResponseEntity.ok("Usuário registrado com sucesso");
    }
}