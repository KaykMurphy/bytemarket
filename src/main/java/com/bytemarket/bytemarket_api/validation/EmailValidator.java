package com.bytemarket.bytemarket_api.validation;

import com.bytemarket.bytemarket_api.exceptions.InvalidEmailException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailValidator {

    private static final String EMAIL_REGEX =
            "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public void validate(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidEmailException("Email não pode ser vazio");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException("Formato de email inválido: " + email);
        }
    }

    public boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}