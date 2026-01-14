package com.bytemarket.bytemarket_api.controllers;

import com.bytemarket.bytemarket_api.dto.response.OrderHistoryDTO;
import com.bytemarket.bytemarket_api.service.UserOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/orders")
@RequiredArgsConstructor
public class UserOrderController {

    private final UserOrderService userOrderService;

    @GetMapping
    public ResponseEntity<List<OrderHistoryDTO>> getUserOrders(@PathVariable UUID userId) {
        List<OrderHistoryDTO> orders = userOrderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderHistoryDTO> getOrderById(
            @PathVariable UUID userId,
            @PathVariable Long orderId
    ) {
        OrderHistoryDTO order = userOrderService.getOrderById(userId, orderId);
        return ResponseEntity.ok(order);
    }
}