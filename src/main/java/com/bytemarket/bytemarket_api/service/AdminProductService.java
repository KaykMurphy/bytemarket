package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.Product;
import com.bytemarket.bytemarket_api.domain.StockItem;
import com.bytemarket.bytemarket_api.dto.request.ProductCreateDTO;
import com.bytemarket.bytemarket_api.dto.request.ProductUpdateDTO;
import com.bytemarket.bytemarket_api.dto.request.StockItemCreateDTO;
import com.bytemarket.bytemarket_api.dto.response.ProductResponseDTO;
import com.bytemarket.bytemarket_api.dto.response.StockStatusDTO;
import com.bytemarket.bytemarket_api.repository.ProductRepository;
import com.bytemarket.bytemarket_api.repository.StockItemRepository;
import com.bytemarket.bytemarket_api.validation.StockValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;
    private final StockValidator stockValidator;

    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO dto) {
        Product product = new Product();
        product.setTitle(dto.title());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setImageUrl(dto.imageUrl());
        product.setType(dto.type());

        Product saved = productRepository.save(product);

        return mapToResponse(saved);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));

        if (dto.title() != null) product.setTitle(dto.title());
        if (dto.description() != null) product.setDescription(dto.description());
        if (dto.price() != null) product.setPrice(dto.price());
        if (dto.imageUrl() != null) product.setImageUrl(dto.imageUrl());
        if (dto.type() != null) product.setType(dto.type());

        if (dto.active() != null) product.setActive(dto.active());

        Product updated = productRepository.save(product);

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));

        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public void addStockItems(Long productId, List<StockItemCreateDTO> items) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + productId));

        for (StockItemCreateDTO itemDto : items) {
            StockItem stockItem = new StockItem();
            stockItem.setContent(itemDto.content());
            stockItem.setSold(false);
            stockItem.setProduct(product);

            stockItemRepository.save(stockItem);
        }
    }

    @Transactional(readOnly = true)
    public StockStatusDTO getStockStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + productId));

        long available = stockValidator.getAvailableStock(product);
        long total = stockItemRepository.count();
        long sold = total - available;

        return new StockStatusDTO(
                product.getId(),
                product.getTitle(),
                available,
                sold,
                total
        );
    }

    private ProductResponseDTO mapToResponse(Product product) {
        long availableStock = stockValidator.getAvailableStock(product);

        return new ProductResponseDTO(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getType(),
                availableStock
        );
    }
}