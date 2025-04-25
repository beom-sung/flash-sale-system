package com.commerce.flashsale.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    @Query(value = """
        SELECT IF(COUNT(*) > 0, 'true', 'false')
        FROM order_history
        WHERE order_history.uuid = :uuid AND order_history.success = true
        """, nativeQuery = true)
    boolean hasSuccessHistory(@Param("uuid") String uuid);

    Integer countBySuccessIsTrue();
}
