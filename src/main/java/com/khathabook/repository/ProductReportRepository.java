package com.khathabook.repository;

import com.khathabook.model.ProductReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReportRepository extends JpaRepository<ProductReport, Long> {
    Long countByProductIdAndStatus(Long productId, String status);
}
