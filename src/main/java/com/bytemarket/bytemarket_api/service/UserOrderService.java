package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.Order;
import com.bytemarket.bytemarket_api.domain.User;
import com.bytemarket.bytemarket_api.dto.response.OrderHistoryDTO;
import com.bytemarket.bytemarket_api.dto.response.OrderHistoryItemDTO;
import com.bytemarket.bytemarket_api.repository.OrderRepository;
import com.bytemarket.bytemarket_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserOrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> getUserOrders(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        List<Order> orders = orderRepository.findByUser(user);

        return orders.stream()
                .map(this::mapToHistoryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderHistoryDTO getOrderById(UUID userId, Long orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        // Verifica se o pedido pertence ao usuário
        if (!order.getUser().getId().equals(userId)) {
            throw new SecurityException("Acesso negado ao pedido");
        }

        return mapToHistoryDTO(order);
    }

    private OrderHistoryDTO mapToHistoryDTO(Order order) {
        List<OrderHistoryItemDTO> items = order.getItems().stream()
                .map(item -> new OrderHistoryItemDTO(
                        item.getProduct().getTitle(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());

        return new OrderHistoryDTO(
                order.getId(),
                order.getMoment(),
                order.getTotal(),
                order.getStatus().name(),
                order.getDeliveryEmail(),
                items
        );
    }
}