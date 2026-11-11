package com.nexusgear.repository.postgres;

import com.nexusgear.model.postgres.OrderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLogRepository extends JpaRepository<OrderLog, Long> {
    List<OrderLog> findTop20ByOrderByCreatedAtDesc();
}
