package com.bytemarket.bytemarket_api.controllers;

import com.bytemarket.bytemarket_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/products")
@RequiredArgsConstructor
public class ProductWebController {

    private final ProductService productService;

    @GetMapping
    public String list(Model model, Pageable pageable) {
        model.addAttribute("products", productService.findAll(pageable));
        return "products/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "products/details";
    }
}