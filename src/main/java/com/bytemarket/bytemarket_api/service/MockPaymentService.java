package com.bytemarket.bytemarket_api.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MockPaymentService implements PaymentStrategy{

    @Override
    public boolean processPayment(BigDecimal amount) {

        // Simulação de pagamento: sempre autorizado.
        // Em produção, substitua por integração com gateway real (Stripe, PagSeguro).
        return true;
    }
}
