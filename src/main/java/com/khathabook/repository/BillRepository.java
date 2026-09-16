package com.khathabook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.khathabook.model.Bill;

import java.time.LocalDate;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    // ================= EXISTING (DO NOT TOUCH) =================

    long countByCustomerIdAndRetailerId(Long customerId, Long retailerId);

    List<Bill> findByCustomerIdAndRetailerId(Long customerId, Long retailerId);

    // ✅ LEGACY SUPPORT
    List<Bill> findByCustomerId(Long customerId);
    // ✅ REFUNDED BILLS
    List<Bill> findByRetailerIdAndRefundedTrue(Long retailerId);


    // ✅ PAID BILLS (NO CUSTOMER)
    List<Bill> findByRetailerIdAndPaidTrue(Long retailerId);
    
    // ✅ ALL BILLS FOR RETAILER (LEGACY)
    List<Bill> findByRetailerIdOrderByBillDateDesc(Long retailerId);

    // ✅ ACTIVE BILLS (NOT DELETED)
    List<Bill> findByRetailerIdAndIsDeletedFalseOrderByBillDateDesc(Long retailerId);

    // ✅ RECYCLED BILLS (SOFT DELETED)
    List<Bill> findByRetailerIdAndIsDeletedTrueOrderByBillDateDesc(Long retailerId);

    // ================= NEW – REPORTING SUPPORT =================

    // ✅ DAILY SALES REPORT
    List<Bill> findByRetailerIdAndBillDate(
            Long retailerId,
            LocalDate billDate
    );

    // ✅ MONTHLY SALES REPORT
    @Query("""
        SELECT b FROM Bill b
        WHERE b.retailer.id = :retailerId
        AND MONTH(b.billDate) = :month
        AND YEAR(b.billDate) = :year
    """)
    List<Bill> findMonthlyReport(
            @Param("retailerId") Long retailerId,
            @Param("month") int month,
            @Param("year") int year
    );
}
