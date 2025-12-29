package repository;

import domain.Product;
import domain.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    /**
     * Busca o primeiro item em estoque de um produto que ainda não foi vendido.
     *
     * @param product o produto cujo item em estoque será procurado
     * @return um {@link Optional} contendo o item encontrado, ou {@link Optional#empty()} se nenhum existir
     */
    Optional<StockItem> findFirstByProductAndSoldFalse(Product product);
}
