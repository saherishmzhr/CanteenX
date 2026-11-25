package com.smartcanteen.repository;

import com.smartcanteen.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT COALESCE(MAX(o.token_no), 0) FROM Order o WHERE o.order_time >= :startOfDay AND o.order_time < :endOfDay")
    Integer findMaxTokenForRange(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer WHERE o.user_id = :userId ORDER BY o.order_time DESC, o.order_id DESC")
    List<Order> findByUserIdOrderByTimeDesc(@Param("userId") Integer userId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer WHERE o.order_id = :orderId")
    Optional<Order> findByIdWithCustomer(@Param("orderId") Integer orderId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer " +
           "WHERE (:startTime IS NULL OR o.order_time >= :startTime) " +
           "AND (:endTime IS NULL OR o.order_time < :endTime) " +
           "AND (:status IS NULL OR :status = '' OR o.status = :status) " +
           "ORDER BY o.order_time DESC, o.order_id DESC")
    List<Order> findFilteredOrders(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime,
                                   @Param("status") String status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer " +
           "WHERE o.order_time >= :startTime AND o.order_time < :endTime " +
           "AND o.status <> 'CANCELLED' " +
           "ORDER BY o.order_time DESC")
    List<Order> findNonCancelledOrdersInRange(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);
}
