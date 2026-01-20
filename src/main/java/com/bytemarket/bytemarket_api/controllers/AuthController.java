package com.bytemarket.bytemarket_api.controllers;

import com.bytemarket.bytemarket_api.domain.User;
import com.bytemarket.bytemarket_api.dto.request.LoginRequestDTO;
import com.bytemarket.bytemarket_api.dto.response.LoginResponseDTO;
import com.bytemarket.bytemarket_api.dto.request.RegisterRequestDTO;
import com.bytemarket.bytemarket_api.dto.response.UserResponseDTO;
import com.bytemarket.bytemarket_api.repository.UserRepository;
import com.bytemarket.bytemarket_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO body) {
        LoginResponseDTO token = authService.login(body);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO body) {
        authService.register(body);
        return ResponseEntity.ok("Usuário registrado com sucesso");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(new UserResponseDTO(user.getId(), user.getName(), user.getEmail()));
    }
}