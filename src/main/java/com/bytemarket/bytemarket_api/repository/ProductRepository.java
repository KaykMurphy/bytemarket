package com.bytemarket.bytemarket_api.repository;

import com.bytemarket.bytemarket_api.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
