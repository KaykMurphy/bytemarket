package com.bytemarket.bytemarket_api.controllers;

import com.bytemarket.bytemarket_api.domain.Order;
import com.bytemarket.bytemarket_api.dto.response.PixPaymentResponseDTO;
import com.bytemarket.bytemarket_api.repository.OrderRepository;
import com.bytemarket.bytemarket_api.service.PixPaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PixPaymentService pixPaymentService;
    private final OrderRepository orderRepository;

    @PostMapping("/pix/orders/{orderId}")
    public ResponseEntity<PixPaymentResponseDTO> createPixPayment(@PathVariable Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        PixPaymentResponseDTO pixData = pixPaymentService.createPixPayment(order);

        return ResponseEntity.ok(pixData);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PixPaymentResponseDTO> getPaymentStatus(@PathVariable Long paymentId) {
        PixPaymentResponseDTO status = pixPaymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(status);
    }
}