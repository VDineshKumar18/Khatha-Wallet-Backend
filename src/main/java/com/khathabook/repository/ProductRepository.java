package com.khathabook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import com.khathabook.model.Product;
import com.khathabook.model.ProductCategory;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Transactional
    @Modifying
    @Query(value = "ALTER TABLE products DROP INDEX UK_qfr8vf85k3q1xinifvsl1eynf", nativeQuery = true)
    void dropLegacyIndex();

    List<Product> findByRetailer_Id(Long retailerId);

    List<Product> findByRetailer_IdAndQuantityLessThanEqual(
            Long retailerId,
            double quantity
    );

    // ✅ SINGLE RESULT (preferred when DB is clean)
    Optional<Product> findByBarcodeAndRetailer_Id(
            String barcode,
            Long retailerId
    );
    
 // ✅ ADD THIS METHOD (do NOT remove anything)
    Optional<Product> findTopByNameIgnoreCase(String name);
    
    // ✅ Fuzzy Search for AI
    Optional<Product> findTopByNameContainingIgnoreCase(String name);


    // ✅ MULTI RESULT (fallback for legacy / bad data)
    List<Product> findAllByBarcodeAndRetailer_Id(
            String barcode,
            Long retailerId
    );

    Optional<Product> findByNameIgnoreCaseAndRetailer_Id(
            String name,
            Long retailerId
    );

    List<Product> findByRetailer_IdAndCategory(
            Long retailerId,
            ProductCategory category
    );

    // ✅ Find products for multiple retailers (e.g. nearby shops)
    List<Product> findByRetailerIn(List<com.khathabook.model.Retailer> retailers);
    
    List<Product> findByRetailer_IdIn(List<Long> retailerIds);

    List<Product> findByApprovalStatus(String status);
}
