package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.*;
import com.bytemarket.bytemarket_api.dto.OrderItemDTO;
import com.bytemarket.bytemarket_api.dto.OrderItemReceiptResponseDTO;
import com.bytemarket.bytemarket_api.dto.OrderReceiptDTO;
import com.bytemarket.bytemarket_api.dto.OrderRequestDTO;
import com.bytemarket.bytemarket_api.repository.OrderRepository;
import com.bytemarket.bytemarket_api.repository.ProductRepository;
import com.bytemarket.bytemarket_api.repository.StockItemRepository;
import com.bytemarket.bytemarket_api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable; 
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentStrategy paymentStrategy;

    @Transactional
    public OrderReceiptDTO placeOrder(OrderRequestDTO dto) {

        UUID userId = dto.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        List<List<String>> codesPerItem = new ArrayList<>();

        for (OrderItemDTO item : dto.items()) {
            Long productId = item.productId();
            Integer quantity = item.quantity();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

            List<String> deliveredCodes = new ArrayList<>();

            if (product.getType() == ProductType.AUTOMATIC_DELIVERY) {

                long availableCount = stockItemRepository.countByProductAndSoldFalse(product);
                if (availableCount < quantity) {
                    throw new IllegalArgumentException("Estoque insuficiente para: " + product.getTitle());
                }

                List<StockItem> stockItems = stockItemRepository.findByProductAndSoldFalse(
                        product,
                        PageRequest.of(0, quantity)
                );

                for (StockItem stockItem : stockItems) {
                    stockItem.setSold(true);
                    stockItemRepository.save(stockItem);
                    deliveredCodes.add(stockItem.getContent());
                }
            }

            codesPerItem.add(deliveredCodes);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            orderItems.add(new OrderItem(null, quantity, product.getPrice(), null, product));
        }

        if (!paymentStrategy.processPayment(total)) {
            throw new IllegalArgumentException("Pagamento falhou.");
        }

        Order order = new Order(null, Instant.now(), total, Status.PAID, user, orderItems);
        order.getItems().forEach(item -> item.setOrder(order));
        Order savedOrder = orderRepository.save(order);

        return buildReceipt(savedOrder, codesPerItem);
    }

    private OrderReceiptDTO buildReceipt(Order order, List<List<String>> codesPerItem) {
        List<OrderItemReceiptResponseDTO> itemsResponse = new ArrayList<>();

        for (int i = 0; i < order.getItems().size(); i++) {
            OrderItem item = order.getItems().get(i);
            List<String> codes = codesPerItem.get(i);

            itemsResponse.add(new OrderItemReceiptResponseDTO(
                    item.getProduct().getTitle(),
                    item.getQuantity(),
                    item.getPrice(),
                    codes
            ));
        }

        return new OrderReceiptDTO(
                order.getId(),
                order.getMoment(),
                order.getTotal(),
                order.getStatus().name(),
                itemsResponse
        );
    }
}
