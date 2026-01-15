package com.bytemarket.bytemarket_api.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MercadoPagoConfiguration {

    @Value("${payment.mercadopago.access-token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            log.info("✅ Mercado Pago configurado com sucesso");
        } catch (Exception e) {
            log.error("❌ Erro ao configurar Mercado Pago: {}", e.getMessage());
            throw new RuntimeException("Falha na configuração do Mercado Pago", e);
        }
    }
}