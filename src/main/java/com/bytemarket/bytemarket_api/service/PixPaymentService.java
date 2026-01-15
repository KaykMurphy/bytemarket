package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.*;
import com.bytemarket.bytemarket_api.dto.response.PixPaymentResponseDTO;
import com.bytemarket.bytemarket_api.repository.PaymentRepository;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class PixPaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${payment.pix.expiration-minutes:30}")
    private int expirationMinutes;

    @Value("${bytemarket.base-url}")
    private String baseUrl;


    @Transactional
    public PixPaymentResponseDTO createPixPayment(com.bytemarket.bytemarket_api.domain.Order order) {

        try {
            Payment mercadoPagoPayment = createMercadoPagoPixPayment(order);

            com.bytemarket.bytemarket_api.domain.Payment payment = savePayment(order, mercadoPagoPayment);

            return mapToDTO(payment);

        } catch (MPApiException e) {
            log.error("❌ Erro API Mercado Pago: {} - {}", e.getStatusCode(), e.getApiResponse().getContent());
            throw new RuntimeException("Erro ao criar pagamento PIX: " + e.getMessage());

        } catch (MPException e) {
            log.error("❌ Erro Mercado Pago: {}", e.getMessage());
            throw new RuntimeException("Erro ao conectar com Mercado Pago: " + e.getMessage());
        }
    }

    /**
     * Cria pagamento PIX no Mercado Pago
     */
    private Payment createMercadoPagoPixPayment(com.bytemarket.bytemarket_api.domain.Order order)
            throws MPException, MPApiException {

        PaymentClient client = new PaymentClient();

        // Data de expiração do PIX
        OffsetDateTime expirationDate = OffsetDateTime.now(ZoneOffset.UTC)
                .plusMinutes(expirationMinutes);

        // Construir requisição
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(order.getTotal())
                .description("Pedido #" + order.getId() + " - ByteMarket")
                .paymentMethodId("pix")
                .dateOfExpiration(expirationDate)
                .notificationUrl(baseUrl + "/webhooks/payment") // URL do webhook
                .externalReference(order.getId().toString()) // Referência do pedido
                .payer(PaymentPayerRequest.builder()
                        .email(order.getDeliveryEmail())
                        .firstName(order.getUser().getName())
                        .build())
                .build();

        log.info("🔄 Criando pagamento PIX no Mercado Pago para pedido {}", order.getId());

        Payment payment = client.create(request);

        log.info("✅ Pagamento PIX criado: ID={}, Status={}",
                payment.getId(), payment.getStatus());

        return payment;
    }

    /**
     * Salva pagamento no banco de dados
     */
    private com.bytemarket.bytemarket_api.domain.Payment savePayment(
            com.bytemarket.bytemarket_api.domain.Order order,
            Payment mercadoPagoPayment) {

        com.bytemarket.bytemarket_api.domain.Payment payment =
                new com.bytemarket.bytemarket_api.domain.Payment();

        payment.setExternalId(mercadoPagoPayment.getId().toString());
        payment.setAmount(order.getTotal());
        payment.setStatus(mapMercadoPagoStatus(mercadoPagoPayment.getStatus()));
        payment.setMethod(PaymentMethod.PIX);
        payment.setOrder(order);
        payment.setExpiresAt(mercadoPagoPayment.getDateOfExpiration().toInstant());

        // Extrair QR Code
        if (mercadoPagoPayment.getPointOfInteraction() != null
                && mercadoPagoPayment.getPointOfInteraction().getTransactionData() != null) {

            payment.setPixQrCode(
                    mercadoPagoPayment.getPointOfInteraction()
                            .getTransactionData().getQrCodeBase64()
            );

            payment.setPixQrCodeText(
                    mercadoPagoPayment.getPointOfInteraction()
                            .getTransactionData().getQrCode()
            );
        }

        return paymentRepository.save(payment);
    }

    /**
     * Mapeia status do Mercado Pago para  enum
     */
    private PaymentStatus mapMercadoPagoStatus(String mpStatus) {
        return switch (mpStatus) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "in_process" -> PaymentStatus.PROCESSING;
            case "pending" -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING;
        };
    }

    /**
     * Mapeia Payment para DTO de resposta
     */
    private PixPaymentResponseDTO mapToDTO(com.bytemarket.bytemarket_api.domain.Payment payment) {
        return new PixPaymentResponseDTO(
                payment.getId(),
                payment.getExternalId(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getPixQrCode(),
                payment.getPixQrCodeText(),
                payment.getExpiresAt(),
                payment.getOrder().getId()
        );
    }

    /**
     * Busca status atual do pagamento no Mercado Pago
     */
    @Transactional(readOnly = true)
    public PixPaymentResponseDTO getPaymentStatus(Long paymentId) {
        com.bytemarket.bytemarket_api.domain.Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        try {
            // Consultar status atualizado no Mercado Pago
            PaymentClient client = new PaymentClient();
            Payment mpPayment = client.get(Long.parseLong(payment.getExternalId()));

            // Atualizar status local se necessário
            PaymentStatus newStatus = mapMercadoPagoStatus(mpPayment.getStatus());
            if (payment.getStatus() != newStatus) {
                payment.setStatus(newStatus);
                paymentRepository.save(payment);
                log.info("Status do pagamento {} atualizado: {}", paymentId, newStatus);
            }

        } catch (Exception e) {
            log.error("Erro ao consultar status no Mercado Pago: {}", e.getMessage());
        }

        return mapToDTO(payment);
    }
}