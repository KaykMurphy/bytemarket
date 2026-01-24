package com.bytemarket.bytemarket_api.controllers;

import com.bytemarket.bytemarket_api.domain.*;
import com.bytemarket.bytemarket_api.repository.OrderRepository;
import com.bytemarket.bytemarket_api.repository.PaymentRepository;
import com.bytemarket.bytemarket_api.repository.StockItemRepository;
import com.bytemarket.bytemarket_api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class TestPaymentController {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StockItemRepository stockItemRepository;
    private final EmailService emailService;

    @GetMapping("/list-payments")
    public ResponseEntity<String> listAllPayments() {
        try {
            List<Payment> payments = paymentRepository.findAll();

            if (payments.isEmpty()) {
                return ResponseEntity.ok("Nenhum pagamento encontrado no banco de dados.");
            }

            StringBuilder result = new StringBuilder("📋 PAGAMENTOS NO SISTEMA:\n\n");

            for (Payment payment : payments) {
                result.append(String.format(
                        "ID: %d | Status: %s | External: %s | Valor: R$ %.2f | Email: %s\n",
                        payment.getId(),
                        payment.getStatus(),
                        payment.getExternalId(),
                        payment.getAmount(),
                        payment.getOrder().getDeliveryEmail()
                ));
            }

            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            log.error("Erro ao listar pagamentos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    @GetMapping("/payment-status/{paymentId}")
    public ResponseEntity<String> checkPaymentStatus(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

            String info = String.format(
                    "Pagamento ID: %d\n" +
                            "Status: %s\n" +
                            "External ID: %s\n" +
                            "Valor: R$ %.2f\n" +
                            "Pedido ID: %d\n" +
                            "Email: %s",
                    payment.getId(),
                    payment.getStatus(),
                    payment.getExternalId(),
                    payment.getAmount(),
                    payment.getOrder().getId(),
                    payment.getOrder().getDeliveryEmail()
            );

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @PostMapping("/approve-payment/{paymentId}")
    public ResponseEntity<String> approvePaymentManually(@PathVariable Long paymentId) {
        try {
            log.info("ESTE: Aprovando pagamento manualmente ID={}", paymentId);

            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

            if (payment.getStatus() == PaymentStatus.APPROVED) {
                return ResponseEntity.ok("Pagamento já estava aprovado!");
            }

            payment.setStatus(PaymentStatus.APPROVED);
            payment.setPaidAt(Instant.now());
            paymentRepository.save(payment);

            Order order = payment.getOrder();
            order.setStatus(Status.PAID);
            orderRepository.save(order);

            List<StockItem> reservedItems = stockItemRepository.findByOrder(order);
            List<List<String>> codesPerItem = new ArrayList<>();

            for (OrderItem orderItem : order.getItems()) {
                List<String> codes = reservedItems.stream()
                        .filter(si -> si.getProduct().getId().equals(orderItem.getProduct().getId()))
                        .map(StockItem::getContent)
                        .collect(Collectors.toList());
                codesPerItem.add(codes);
            }

            log.info("📧 Enviando email para: {}", order.getDeliveryEmail());
            emailService.sendOrderConfirmation(order, codesPerItem);

            String message = String.format(
                    "Pagamento #%d aprovado com sucesso!\n" +
                            "Pedido #%d atualizado para PAID\n" +
                            " Email enviado para: %s\n" +
                            "%d produtos entregues",
                    paymentId, order.getId(), order.getDeliveryEmail(), reservedItems.size()
            );

            log.info(message);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("Erro ao aprovar pagamento manualmente: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Erro: " + e.getMessage());
        }
    }
}