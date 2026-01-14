package com.bytemarket.bytemarket_api.validation;

import com.bytemarket.bytemarket_api.domain.Product;
import com.bytemarket.bytemarket_api.exceptions.OutOfStockException;
import com.bytemarket.bytemarket_api.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockValidator {

    private final StockItemRepository stockItemRepository;

    public void validateAvailability(Product product, int requestedQuantity) {
        long available = stockItemRepository.countByProductAndSoldFalse(product);

        if (available < requestedQuantity) {
            throw new OutOfStockException(
                    String.format(
                            "Estoque insuficiente para '%s'. Disponível: %d, Solicitado: %d",
                            product.getTitle(),
                            available,
                            requestedQuantity
                    )
            );
        }
    }

    public long getAvailableStock(Product product) {
        return stockItemRepository.countByProductAndSoldFalse(product);
    }
}