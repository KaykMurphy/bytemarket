package com.bytemarket.bytemarket_api.repository;

import com.bytemarket.bytemarket_api.domain.Product;
import com.bytemarket.bytemarket_api.domain.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    /**
     * Busca o primeiro item em estoque de um produto que ainda não foi vendido.
     *
     * @param product o produto cujo item em estoque será procurado
     * @return um {@link Optional} contendo o item encontrado, ou {@link Optional#empty()} se nenhum existir
     */
    Optional<StockItem> findFirstByProductAndSoldFalse(Product product);

    List<StockItem> findByProductAndSoldFalse(Product product, Pageable pageable);

    long countByProductAndSoldFalse(Product product);
}
