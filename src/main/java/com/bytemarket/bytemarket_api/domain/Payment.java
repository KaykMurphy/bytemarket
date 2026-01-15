package com.bytemarket.bytemarket_api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments_tb")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String externalId; // ID do gateway de pagamento

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(columnDefinition = "TEXT")
    private String pixQrCode; // QR Code do PIX (base64 ou URL)

    @Column(columnDefinition = "TEXT")
    private String pixQrCodeText; // Código copia-cola do PIX

    private Instant createdAt;
    private Instant paidAt;
    private Instant expiresAt;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}