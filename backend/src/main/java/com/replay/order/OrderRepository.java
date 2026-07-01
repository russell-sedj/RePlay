package com.replay.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);
    List<Order> findByUserId(Long userId);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'CONFIRMED'")
    BigDecimal sumTotalAmountByStatusConfirmed();

    @Query("SELECT oi.product.name, oi.product.slug, SUM(oi.quantity) as totalSold " +
           "FROM OrderItem oi JOIN oi.order o WHERE o.status = 'CONFIRMED' " +
           "GROUP BY oi.product.name, oi.product.slug ORDER BY totalSold DESC")
    List<Object[]> findTopProductsByQuantitySold(Pageable pageable);
}
