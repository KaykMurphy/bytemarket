package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.*;
import com.bytemarket.bytemarket_api.dto.request.OrderItemDTO;
import com.bytemarket.bytemarket_api.dto.response.OrderItemReceiptResponseDTO;
import com.bytemarket.bytemarket_api.dto.response.OrderReceiptDTO;
import com.bytemarket.bytemarket_api.dto.request.OrderRequestDTO;
import com.bytemarket.bytemarket_api.repository.OrderRepository;
import com.bytemarket.bytemarket_api.repository.ProductRepository;
import com.bytemarket.bytemarket_api.repository.StockItemRepository;
import com.bytemarket.bytemarket_api.repository.UserRepository;
import com.bytemarket.bytemarket_api.validation.EmailValidator;
import com.bytemarket.bytemarket_api.validation.StockValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EmailValidator emailValidator;
    private final StockValidator stockValidator;

    @Transactional
    public OrderReceiptDTO placeOrder(OrderRequestDTO dto) {

        emailValidator.validate(dto.deliveryEmail());

        UUID userId = dto.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // Criar o objeto Order primeiro para poder vincular os itens de estoque a ele
        Order order = new Order();
        order.setMoment(Instant.now());
        order.setStatus(Status.WAITING_PAYMENT);
        order.setDeliveryEmail(dto.deliveryEmail());
        order.setUser(user);

        // Salva o pedido preliminarmente para ter o ID/Referência
        Order savedOrder = orderRepository.save(order);

        // Processar itens
        for (OrderItemDTO item : dto.items()) {
            Long productId = item.productId();
            Integer quantity = item.quantity();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

            boolean isAutomaticDelivery =
                    product.getType() == ProductType.AUTOMATIC_DELIVERY ||
                            product.getType() == ProductType.ASSINATURAS ||
                            product.getType() == ProductType.KEYS ||
                            product.getType() == ProductType.CONTAS ||
                            product.getType() == ProductType.STREAMING ||
                            product.getType() == ProductType.DISCORD ||
                            product.getType() == ProductType.UTILIDADES ||
                            product.getType() == ProductType.METODOS;

            if (isAutomaticDelivery) {
                stockValidator.validateAvailability(product, quantity);

                List<StockItem> stockItems = stockItemRepository.findByProductAndSoldFalse(
                        product,
                        PageRequest.of(0, quantity)
                );

                for (StockItem stockItem : stockItems) {
                    stockItem.setSold(true); // Marca como vendido (reservado)
                    stockItem.setOrder(savedOrder); // VINCULA AO PEDIDO
                    stockItemRepository.save(stockItem);
                }
            }

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            orderItems.add(new OrderItem(null, quantity, product.getPrice(), savedOrder, product));
        }

        // Atualiza totais e itens do pedido
        savedOrder.setTotal(total);
        savedOrder.setItems(orderItems);
        orderRepository.save(savedOrder);

        return buildReceipt(savedOrder);
    }

    private OrderReceiptDTO buildReceipt(Order order) {
        List<OrderItemReceiptResponseDTO> itemsResponse = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            itemsResponse.add(new OrderItemReceiptResponseDTO(
                    item.getProduct().getTitle(),
                    item.getQuantity(),
                    item.getPrice(),
                    null
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