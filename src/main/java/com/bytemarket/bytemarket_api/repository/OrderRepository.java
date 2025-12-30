package com.bytemarket.bytemarket_api.repository;

import com.bytemarket.bytemarket_api.domain.Order;
import com.bytemarket.bytemarket_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
