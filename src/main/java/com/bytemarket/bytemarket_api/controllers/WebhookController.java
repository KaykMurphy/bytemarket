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
@CrossOrigin(origins = "*") // Permite ngrok
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/payment")
    public ResponseEntity<Void> handlePaymentWebhook(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestParam(value = "id", required = false) String id,
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestBody(required = false) String rawBody
    ) {
        String finalId = (dataId != null) ? dataId : id;
        String finalType = (type != null) ? type : "payment";

        log.info("=== WEBHOOK RECEBIDO ===");
        log.info("Type: {}", finalType);
        log.info("Data ID: {}", finalId);
        log.info("Signature: {}", xSignature);
        log.info("Request ID: {}", xRequestId);
        log.info("Raw Body: {}", rawBody);

        if (finalId == null) {
            log.warn("Webhook ignorado: ID do pagamento não encontrado.");
            return ResponseEntity.ok().build();
        }

        try {
            webhookService.processMercadoPagoWebhook(finalType, finalId, xSignature, xRequestId);
            log.info("Webhook processado com sucesso!");
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            log.error("Assinatura inválida: {}", e.getMessage());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao processar webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}