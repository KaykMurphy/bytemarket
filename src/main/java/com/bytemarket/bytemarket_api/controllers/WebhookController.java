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
    public ResponseEntity<Void> handlePaymentWebhook(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestParam(value = "id", required = false) String id, // Fallback
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId
    ) {
        String finalId = (dataId != null) ? dataId : id;

        String finalType = (type != null) ? type : "payment";

        log.info("Webhook Mercado Pago recebido: type={}, dataId={}", finalType, finalId);

        if (finalId == null) {
            log.warn("Webhook ignorado: ID do pagamento não encontrado.");
            return ResponseEntity.ok().build();
        }

        try {
            webhookService.processMercadoPagoWebhook(finalType, finalId, xSignature, xRequestId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            log.error("Assinatura inválida: {}", e.getMessage());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro interno no webhook: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}