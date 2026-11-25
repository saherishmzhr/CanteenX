package com.smartcanteen.repository;

import com.smartcanteen.model.OrderDetail;
import com.smartcanteen.model.PopularItemReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    @Query("SELECT new com.smartcanteen.model.PopularItemReport(" +
           "d.item_id, d.item_name, SUM(d.quantity), SUM(d.subtotal)) " +
           "FROM OrderDetail d JOIN d.order o " +
           "WHERE o.status <> 'CANCELLED' " +
           "AND (:startTime IS NULL OR o.order_time >= :startTime) " +
           "AND (:endTime IS NULL OR o.order_time < :endTime) " +
           "GROUP BY d.item_id, d.item_name " +
           "ORDER BY SUM(d.quantity) DESC, SUM(d.subtotal) DESC")
    List<PopularItemReport> findPopularItems(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
}
