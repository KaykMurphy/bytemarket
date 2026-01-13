package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.Role;
import com.bytemarket.bytemarket_api.domain.User;
import com.bytemarket.bytemarket_api.dto.LoginRequestDTO;
import com.bytemarket.bytemarket_api.dto.LoginResponseDTO;
import com.bytemarket.bytemarket_api.dto.RegisterRequestDTO;
import com.bytemarket.bytemarket_api.repository.UserRepository;
import com.bytemarket.bytemarket_api.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow();

        String roleName = user.getRole().name();

        String token = jwtUtils.generateToken(user.getEmail(), roleName);

        return new LoginResponseDTO(token);
    }

    public User register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        User newUser = new User();
        newUser.setName(request.name());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));

        newUser.setRole(Role.USER);

        return userRepository.save(newUser);
    }
}