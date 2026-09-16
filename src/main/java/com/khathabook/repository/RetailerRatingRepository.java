package com.khathabook.repository;

import com.khathabook.model.RetailerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetailerRatingRepository extends JpaRepository<RetailerRating, Long> {
    
    List<RetailerRating> findByRetailerId(Long retailerId);
    
    Optional<RetailerRating> findByOrderId(Long orderId);
    
    List<RetailerRating> findByCustomerId(Long customerId);

    @Query("SELECT AVG(r.storeRating) FROM RetailerRating r WHERE r.retailerId = :retailerId")
    Double getAverageStoreRating(@Param("retailerId") Long retailerId);

    @Query("SELECT COUNT(r) FROM RetailerRating r WHERE r.retailerId = :retailerId")
    Long countRatingsByRetailerId(@Param("retailerId") Long retailerId);
}
