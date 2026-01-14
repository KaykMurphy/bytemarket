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
    private final PaymentStrategy paymentStrategy;
    private final EmailValidator emailValidator;
    private final StockValidator stockValidator;

    @Transactional
    public OrderReceiptDTO placeOrder(OrderRequestDTO dto) {

        // Validar email de entrega
        emailValidator.validate(dto.deliveryEmail());

        // Buscar usuário
        UUID userId = dto.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        List<List<String>> codesPerItem = new ArrayList<>();

        // Processar cada item do pedido
        for (OrderItemDTO item : dto.items()) {
            Long productId = item.productId();
            Integer quantity = item.quantity();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

            List<String> deliveredCodes = new ArrayList<>();

            // Se for entrega automática, validar e reservar estoque
            if (product.getType() == ProductType.AUTOMATIC_DELIVERY) {

                // Validar estoque disponível ANTES de processar
                stockValidator.validateAvailability(product, quantity);

                // Reservar itens do estoque
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

            // Calcular total
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));

            // Criar item do pedido
            orderItems.add(new OrderItem(null, quantity, product.getPrice(), null, product));
        }

        // Processar pagamento
        if (!paymentStrategy.processPayment(total)) {
            throw new RuntimeException("Pagamento não autorizado");
        }

        // Criar pedido
        Order order = new Order();
        order.setMoment(Instant.now());
        order.setTotal(total);
        order.setStatus(Status.PAID);
        order.setDeliveryEmail(dto.deliveryEmail());
        order.setUser(user);
        order.setItems(orderItems);

        // Associar items ao pedido
        order.getItems().forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        // TODO: Enviar email com as contas (implementar EmailService)

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