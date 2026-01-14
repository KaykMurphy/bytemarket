package com.bytemarket.bytemarket_api.controllers;

import com.bytemarket.bytemarket_api.dto.response.ProductResponseDTO;
import com.bytemarket.bytemarket_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping()
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(Pageable pageable){

       Page<ProductResponseDTO> result = productService.findAll(pageable);

       return ResponseEntity.ok(result);
    }
}
