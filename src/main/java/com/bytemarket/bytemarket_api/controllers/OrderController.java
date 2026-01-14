package com.bytemarket.bytemarket_api.controllers;

import com.bytemarket.bytemarket_api.dto.response.OrderReceiptDTO;
import com.bytemarket.bytemarket_api.dto.request.OrderRequestDTO;
import com.bytemarket.bytemarket_api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderReceiptDTO> createOrder(@Valid @RequestBody OrderRequestDTO dto) {
        OrderReceiptDTO result = orderService.placeOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}