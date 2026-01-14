package com.bytemarket.bytemarket_api.controllers;

import com.bytemarket.bytemarket_api.dto.request.ProductCreateDTO;
import com.bytemarket.bytemarket_api.dto.request.ProductUpdateDTO;
import com.bytemarket.bytemarket_api.dto.request.StockItemCreateDTO;
import com.bytemarket.bytemarket_api.dto.response.ProductResponseDTO;
import com.bytemarket.bytemarket_api.dto.response.StockStatusDTO;
import com.bytemarket.bytemarket_api.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductCreateDTO dto
    ) {
        ProductResponseDTO result = adminProductService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDTO dto
    ) {
        ProductResponseDTO result = adminProductService.updateProduct(id, dto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/stock")
    public ResponseEntity<String> addStockItems(
            @PathVariable Long productId,
            @Valid @RequestBody List<StockItemCreateDTO> items
    ) {
        adminProductService.addStockItems(productId, items);
        return ResponseEntity.ok(items.size() + " itens adicionados ao estoque");
    }

    @GetMapping("/{productId}/stock/status")
    public ResponseEntity<StockStatusDTO> getStockStatus(@PathVariable Long productId) {
        StockStatusDTO status = adminProductService.getStockStatus(productId);
        return ResponseEntity.ok(status);
    }
}