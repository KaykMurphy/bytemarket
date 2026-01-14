package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.Role;
import com.bytemarket.bytemarket_api.domain.User;
import com.bytemarket.bytemarket_api.dto.request.LoginRequestDTO;
import com.bytemarket.bytemarket_api.dto.response.LoginResponseDTO;
import com.bytemarket.bytemarket_api.dto.request.RegisterRequestDTO;
import com.bytemarket.bytemarket_api.exceptions.DuplicateEmailException;
import com.bytemarket.bytemarket_api.repository.UserRepository;
import com.bytemarket.bytemarket_api.security.JwtUtils;
import com.bytemarket.bytemarket_api.validation.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final EmailValidator emailValidator;

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String roleName = user.getRole().name();
        String token = jwtUtils.generateToken(user.getEmail(), roleName);

        return new LoginResponseDTO(token);
    }

    @Transactional
    public User register(RegisterRequestDTO request) {
        // Valida formato do email
        emailValidator.validate(request.email());

        // Verifica duplicidade
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateEmailException("Email já cadastrado: " + request.email());
        }

        User newUser = new User();
        newUser.setName(request.name());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(Role.USER);

        return userRepository.save(newUser);
    }
}