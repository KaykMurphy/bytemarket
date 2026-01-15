package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.*;
import com.bytemarket.bytemarket_api.dto.response.PixPaymentResponseDTO;
import com.bytemarket.bytemarket_api.repository.OrderRepository;
import com.bytemarket.bytemarket_api.repository.ProductRepository;
import com.bytemarket.bytemarket_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PixPaymentIntegrationTest {

    @Autowired
    private PixPaymentService pixPaymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Criar usuário de teste
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@bytemarket.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);

        // Criar produto de teste
        testProduct = new Product();
        testProduct.setTitle("Conta Netflix Premium");
        testProduct.setDescription("Conta Netflix Premium - 1 Mês");
        testProduct.setPrice(new BigDecimal("49.90"));
        testProduct.setImageUrl("https://example.com/netflix.png");
        testProduct.setType(ProductType.AUTOMATIC_DELIVERY);
        testProduct = productRepository.save(testProduct);

        // Criar pedido de teste
        testOrder = new Order();
        testOrder.setMoment(Instant.now());
        testOrder.setTotal(new BigDecimal("49.90"));
        testOrder.setStatus(Status.WAITING_PAYMENT);
        testOrder.setDeliveryEmail("test@bytemarket.com");
        testOrder.setUser(testUser);
        testOrder.setItems(new ArrayList<>());
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void testCreatePixPayment() {
        // Criar pagamento PIX
        PixPaymentResponseDTO response = pixPaymentService.createPixPayment(testOrder);

        // Verificações
        assertNotNull(response);
        assertNotNull(response.paymentId());
        assertNotNull(response.externalId());
        assertEquals(new BigDecimal("49.90"), response.amount());
        assertEquals("PENDING", response.status());
        assertNotNull(response.pixQrCode());
        assertNotNull(response.pixQrCodeText());
        assertNotNull(response.expiresAt());
        assertEquals(testOrder.getId(), response.orderId());

        System.out.println("✅ Pagamento PIX criado com sucesso!");
        System.out.println("📱 External ID: " + response.externalId());
        System.out.println("💰 Valor: R$ " + response.amount());
        System.out.println("📋 QR Code Texto: " + response.pixQrCodeText().substring(0, 50) + "...");
    }

    @Test
    void testGetPaymentStatus() {
        // Criar pagamento
        PixPaymentResponseDTO created = pixPaymentService.createPixPayment(testOrder);

        // Buscar status
        PixPaymentResponseDTO status = pixPaymentService.getPaymentStatus(created.paymentId());

        // Verificações
        assertNotNull(status);
        assertEquals(created.paymentId(), status.paymentId());
        assertEquals(created.externalId(), status.externalId());

        System.out.println("✅ Status consultado com sucesso!");
        System.out.println("📊 Status atual: " + status.status());
    }
}