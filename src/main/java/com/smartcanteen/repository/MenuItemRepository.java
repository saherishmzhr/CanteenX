package com.smartcanteen.repository;

import com.smartcanteen.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

    @Query("SELECT m FROM MenuItem m WHERE m.active = true " +
           "AND (:category IS NULL OR :category = '' OR UPPER(m.category) = UPPER(:category)) " +
           "AND (:search IS NULL OR :search = '' OR LOWER(m.item_name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY m.item_id ASC")
    List<MenuItem> searchActiveMenu(@Param("category") String category, @Param("search") String search);

    @Query("SELECT m FROM MenuItem m ORDER BY m.active DESC, m.item_id ASC")
    List<MenuItem> findAllOrdered();
}
