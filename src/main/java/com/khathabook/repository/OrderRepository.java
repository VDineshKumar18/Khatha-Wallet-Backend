package com.khathabook.repository;

import com.khathabook.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);
    List<Order> findByRetailerIdOrderByOrderDateDesc(Long retailerId);
    List<Order> findByRetailerIdAndStatus(Long retailerId, String status);

    // State machine lookups
    List<Order> findByStatusAndOrderDateBefore(String status, java.time.LocalDateTime date);
    List<Order> findByStatusAndReadyForPickupAtBefore(String status, java.time.LocalDateTime date);
}
