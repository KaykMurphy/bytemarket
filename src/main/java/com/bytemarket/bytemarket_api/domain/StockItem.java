package com.bytemarket.bytemarket_api.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stockItems_tb")
public class StockItem { //O item digital real (segredo/login)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content; // // Login/Senha

    private Boolean sold = false; // indica se já foi vendido

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Version
    private Long version;
}


