package com.bytemarket.bytemarket_api.controllers;

import com.bytemarket.bytemarket_api.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/payment")
    public ResponseEntity<String> receiveMercadoPagoWebhook(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId
    ) {
        log.info("📨 Webhook Mercado Pago recebido: type={}, dataId={}", type, dataId);

        try {
            webhookService.processMercadoPagoWebhook(type, dataId, xSignature, xRequestId);
            return ResponseEntity.ok("Webhook processado com sucesso");

        } catch (SecurityException e) {
            log.error("🔒 Assinatura inválida: {}", e.getMessage());
            return ResponseEntity.status(401).body("Assinatura inválida");

        } catch (Exception e) {
            log.error("❌ Erro ao processar webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erro ao processar webhook");
        }
    }

    /**
     * Endpoint de health check para testar se o webhook está acessível
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Webhook endpoint ativo");
    }
}