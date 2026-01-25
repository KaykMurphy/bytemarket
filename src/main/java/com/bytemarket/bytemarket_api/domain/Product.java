package com.bytemarket.bytemarket_api.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products_tb")
public class Product { //A vitrine (o que o usuário vê)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String imageUrl;


    @Enumerated(EnumType.STRING)
    private ProductType type;

    @OneToMany(mappedBy = "product")
    private List<StockItem> stockItems = new ArrayList<>();

    private Boolean active = true;

}

