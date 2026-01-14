package com.bytemarket.bytemarket_api.service;

import com.bytemarket.bytemarket_api.domain.Product;
import com.bytemarket.bytemarket_api.dto.response.ProductResponseDTO;
import com.bytemarket.bytemarket_api.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;


    @Transactional
    public Page<ProductResponseDTO> findAll(Pageable pageable){

        return productRepository.findAll(pageable)
                .map(product -> new ProductResponseDTO(
                        product.getId(),
                        product.getTitle(),
                        product.getPrice(),
                        product.getImageUrl(),
                        product.getType()
                ));
    }

    @Transactional
    public ProductResponseDTO findById(Long id){

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: "+id));

        return new ProductResponseDTO(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getImageUrl(),
                product.getType()
        );

    }


}
