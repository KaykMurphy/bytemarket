package com.bytemarket.bytemarket_api.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ValidationError {
    private Instant timestamp;
    private Integer status;
    private String error;
    private Map<String, String> errors; // Campo -> Mensagem de erro
    private String path;
}