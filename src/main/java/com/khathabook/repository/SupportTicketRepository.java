package com.khathabook.repository;

import com.khathabook.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByReporterTypeAndReporterId(String reporterType, Long reporterId);
    List<SupportTicket> findByStatus(String status);
    List<SupportTicket> findByTicketType(String ticketType);
}
