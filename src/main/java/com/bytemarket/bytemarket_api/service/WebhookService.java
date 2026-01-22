package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.*;
import com.bytemarket.bytemarket_api.repository.OrderRepository;
import com.bytemarket.bytemarket_api.repository.PaymentRepository;
import com.bytemarket.bytemarket_api.repository.StockItemRepository;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StockItemRepository stockItemRepository; // Novo repository
    private final EmailService emailService;

    @Value("${payment.mercadopago.webhook-secret:}")
    private String webhookSecret;

    @Transactional
    public void processMercadoPagoWebhook(String type, String dataId, String xSignature, String xRequestId) {
        log.info("📨 Webhook recebido: type={}, dataId={}", type, dataId);

        if (!validateMercadoPagoSignature(xSignature, xRequestId, dataId)) {
            log.warn("⚠️ Webhook com assinatura inválida rejeitado");
            throw new SecurityException("Assinatura do webhook inválida");
        }

        if (!"payment".equals(type)) return;

        try {
            PaymentClient client = new PaymentClient();
            Payment mpPayment = client.get(Long.parseLong(dataId));

            com.bytemarket.bytemarket_api.domain.Payment payment =
                    paymentRepository.findByExternalId(dataId)
                            .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado: " + dataId));

            processPaymentStatusChange(payment, mpPayment);

        } catch (Exception e) {
            log.error("❌ Erro ao processar webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar webhook", e);
        }
    }

    private void processPaymentStatusChange(com.bytemarket.bytemarket_api.domain.Payment payment, Payment mpPayment) {
        String mpStatus = mpPayment.getStatus();
        PaymentStatus newStatus = mapStatus(mpStatus);

        if (payment.getStatus() == newStatus) return;

        payment.setStatus(newStatus);

        if (newStatus == PaymentStatus.APPROVED) {
            payment.setPaidAt(mpPayment.getDateApproved().toInstant());
            Order order = payment.getOrder();
            order.setStatus(Status.PAID);
            orderRepository.save(order);

            // LOGICA DE ENTREGA DE PRODUTOS
            try {
                // 1. Busca os itens reservados para este pedido
                List<StockItem> reservedItems = stockItemRepository.findByOrder(order);

                // 2. Organiza os códigos para envio (List<List<String>>)
                List<List<String>> codesPerItem = new ArrayList<>();

                for (OrderItem orderItem : order.getItems()) {
                    List<String> codes = reservedItems.stream()
                            .filter(si -> si.getProduct().getId().equals(orderItem.getProduct().getId()))
                            .map(StockItem::getContent)
                            .collect(Collectors.toList());
                    codesPerItem.add(codes);
                }

                // 3. Envia o email com os produtos
                log.info("🚀 Pagamento Aprovado! Enviando produtos para {}", order.getDeliveryEmail());
                emailService.sendOrderConfirmation(order, codesPerItem);

            } catch (Exception e) {
                log.error("❌ Erro ao entregar produtos: {}", e.getMessage());
            }
        }

        paymentRepository.save(payment);
    }

    private boolean validateMercadoPagoSignature(String xSignature, String xRequestId, String dataId) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("⚠️ Webhook secret não configurado");
            return true;
        }

        if (xSignature == null || xSignature.isBlank()) {
            return false;
        }

        try {
            String[] parts = xSignature.split(",");
            String ts = null;
            String hash = null;

            for (String part : parts) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length == 2) {
                    if ("ts".equals(keyValue[0])) {
                        ts = keyValue[1];
                    } else if ("v1".equals(keyValue[0])) {
                        hash = keyValue[1];
                    }
                }
            }

            if (ts == null || hash == null) {
                return false;
            }

            String manifest = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";
            String expectedHash = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, webhookSecret)
                    .hmacHex(manifest);

            return expectedHash.equals(hash);

        } catch (Exception e) {
            log.error("❌ Erro ao validar assinatura: {}", e.getMessage());
            return false;
        }
    }

    private PaymentStatus mapStatus(String mpStatus) {
        return switch (mpStatus) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "in_process" -> PaymentStatus.PROCESSING;
            case "pending" -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING;
        };
    }
}