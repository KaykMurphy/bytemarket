package com.bytemarket.bytemarket_api.controllers;

import com.bytemarket.bytemarket_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping("/")
    public String home(Model model, Pageable pageable) {
        model.addAttribute("products", productService.findAll(pageable));
        return "index";

}
}