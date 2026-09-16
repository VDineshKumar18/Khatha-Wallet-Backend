package com.khathabook.service;

import com.khathabook.model.Order;
import com.khathabook.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderStateMachineScheduledTasks {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BillService billService;

    @Autowired
    private NotificationService notificationService;

    // Run every minute
    @Scheduled(fixedRate = 60000)
    public void processAutoCancelAndExpire() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Auto-Cancel PLACED orders older than 15 minutes
        LocalDateTime cancelThreshold = now.minusMinutes(15);
        List<Order> unacceptedOrders = orderRepository.findByStatusAndOrderDateBefore("PLACED", cancelThreshold);
        for (Order order : unacceptedOrders) {
            order.setStatus("AUTO_CANCELLED");
            orderRepository.save(order);
            System.out.println("Auto-cancelled order: " + order.getId());
            notificationService.sendOrderStatusEmail(order, order.getCustomer());
        }

        // 2. Expire READY_FOR_PICKUP orders older than 12 hours
        LocalDateTime expireThreshold = now.minusHours(12);
        List<Order> abandonedOrders = orderRepository.findByStatusAndReadyForPickupAtBefore("READY_FOR_PICKUP", expireThreshold);
        for (Order order : abandonedOrders) {
            order.setStatus("EXPIRED");
            orderRepository.save(order);
            restoreStock(order);
            System.out.println("Expired abandoned order: " + order.getId());
            notificationService.sendOrderStatusEmail(order, order.getCustomer());
        }
    }

    private void restoreStock(Order order) {
        try {
            billService.restoreStock(order.getItems(), order.getRetailer().getId());
        } catch (Exception e) {
            System.err.println("Stock Restoration Failed during Expire: " + e.getMessage());
        }
    }
}
