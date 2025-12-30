package com.bytemarket.bytemarket_api.service;

import java.math.BigDecimal;

public interface PaymentStrategy{

    boolean processPayment(BigDecimal amount);
}
