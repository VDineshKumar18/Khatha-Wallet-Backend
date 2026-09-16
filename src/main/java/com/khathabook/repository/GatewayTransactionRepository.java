package com.khathabook.repository;

import com.khathabook.model.GatewayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GatewayTransactionRepository extends JpaRepository<GatewayTransaction, Long> {
    List<GatewayTransaction> findByRetailerId(Long retailerId);
    Optional<GatewayTransaction> findByTransactionRef(String transactionRef);
}
