package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.Product;
import com.bytemarket.bytemarket_api.dto.response.ProductResponseDTO;
import com.bytemarket.bytemarket_api.repository.ProductRepository;
import com.bytemarket.bytemarket_api.validation.StockValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StockValidator stockValidator;

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new EntityNotFoundException("Este produto foi removido ou está indisponível.");
        }

        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchByTitle(String query) {
        List<Product> products = productRepository.findByTitleContainingIgnoreCase(query);

        return products.stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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