package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.*;
import com.bytemarket.bytemarket_api.dto.response.PixPaymentResponseDTO;
import com.bytemarket.bytemarket_api.repository.PaymentRepository;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;


    @Transactional
    public PixPaymentResponseDTO createPixPayment(com.bytemarket.bytemarket_api.domain.Order order) {

        try {
            Payment mercadoPagoPayment = createMercadoPagoPixPayment(order);

            com.bytemarket.bytemarket_api.domain.Payment payment = savePayment(order, mercadoPagoPayment);

            return mapToDTO(payment);

        } catch (MPApiException e) {
            log.error("Erro API Mercado Pago: {} - {}", e.getStatusCode(), e.getApiResponse().getContent());
            // Mostra o erro real da API
            throw new RuntimeException("Erro na API de Pagamento: " + e.getApiResponse().getContent());

        } catch (MPException e) {
            log.error("Erro Mercado Pago: {}", e.getMessage());
            throw new RuntimeException("Erro de conexão com Pagamento: " + e.getMessage());
        }
    }

    private Payment createMercadoPagoPixPayment(com.bytemarket.bytemarket_api.domain.Order order)
            throws MPException, MPApiException {

        PaymentClient client = new PaymentClient();

        OffsetDateTime expirationDate = OffsetDateTime.now(ZoneOffset.UTC)
                .plusMinutes(expirationMinutes);

        BigDecimal amount = order.getTotal().setScale(2, RoundingMode.HALF_EVEN);

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(amount) // Envia o valor (ex: 49.90)
                .description("Pedido #" + order.getId() + " - ByteMarket")
                .paymentMethodId("pix")
                .dateOfExpiration(expirationDate)
                .notificationUrl(baseUrl + "/webhooks/payment")
                .externalReference(order.getId().toString())
                .payer(PaymentPayerRequest.builder()
                        .email(order.getDeliveryEmail())
                        .firstName(order.getUser().getName())
                        .build())
                .build();

        log.info("Criando pagamento PIX de R$ {}", amount);

        Payment payment = client.create(request);

        log.info("Pagamento PIX criado: ID={}, Status={}",
                payment.getId(), payment.getStatus());

        return payment;
    }

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

    private PaymentStatus mapMercadoPagoStatus(String mpStatus) {
        if (mpStatus == null) return PaymentStatus.PENDING;

        return switch (mpStatus) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "in_process" -> PaymentStatus.PROCESSING;
            case "pending" -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING;
        };
    }

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

    @Transactional
    public PixPaymentResponseDTO getPaymentStatus(Long paymentId) {
        com.bytemarket.bytemarket_api.domain.Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));

        // Se estiver em desenvolvimento e o pagamento já foi aprovado manualmente
        if ("dev".equals(activeProfile) && payment.getStatus() == PaymentStatus.APPROVED) {
            return mapToDTO(payment);
        }

        try {
            PaymentClient client = new PaymentClient();
            Payment mpPayment = client.get(Long.parseLong(payment.getExternalId()));

            PaymentStatus newStatus = mapMercadoPagoStatus(mpPayment.getStatus());

            if (payment.getStatus() != newStatus) {
                payment.setStatus(newStatus);
                paymentRepository.save(payment);
                log.info("Status do pagamento {} atualizado via API: {}", paymentId, newStatus);
            }

        } catch (Exception e) {
            log.error("Erro ao consultar status no Mercado Pago: {}", e.getMessage());
        }

        return mapToDTO(payment);
    }
}
