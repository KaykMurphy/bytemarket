package com.bytemarket.bytemarket_api.repository;

import com.bytemarket.bytemarket_api.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByExternalId(String externalId);

    Optional<Payment> findByOrderId(Long orderId);
}