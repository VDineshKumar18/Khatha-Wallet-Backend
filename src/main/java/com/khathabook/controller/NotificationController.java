package com.khathabook.controller;

import com.khathabook.service.NotificationService;
import com.khathabook.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final WhatsAppService whatsappService;
    private final com.khathabook.repository.OrderRepository orderRepo;
    private final com.khathabook.repository.CustomerRepository customerRepo;
    private final com.khathabook.repository.BillRepository billRepo;

    public NotificationController(
            NotificationService notificationService,
            WhatsAppService whatsappService,
            com.khathabook.repository.OrderRepository orderRepo,
            com.khathabook.repository.CustomerRepository customerRepo,
            com.khathabook.repository.BillRepository billRepo
    ) {
        this.notificationService = notificationService;
        this.whatsappService = whatsappService;
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.billRepo = billRepo;
    }

    @PostMapping("/{customerId}")
    public ResponseEntity<String> notifyCustomer(
            @PathVariable Long customerId,
            @RequestHeader("X-Retailer-Id") Long retailerId
    ) {
        notificationService.sendDueAmountEmail(customerId, retailerId);
        return ResponseEntity.ok("Notification sent");
    }

    // ======================================================
    // ✅ PERSONAL WHATSAPP LINKS (wa.me)
    // ======================================================

    @GetMapping("/whatsapp/order/{orderId}")
    public ResponseEntity<?> getOrderWhatsAppLink(@PathVariable Long orderId) {
        return orderRepo.findById(orderId)
                .map(order -> {
                    if (order.getCustomer() == null) {
                        return ResponseEntity.badRequest().body("Customer is not associated with this order.");
                    }
                    if (order.getCustomer().getPhone() == null || order.getCustomer().getPhone().isBlank()) {
                        return ResponseEntity.badRequest().body("Customer's phone number is missing.");
                    }
                    String msg = whatsappService.generateOrderUpdateMessage(order);
                    String link = whatsappService.getWhatsAppLink(order.getCustomer().getPhone(), msg);
                    return ResponseEntity.ok(link);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/whatsapp/due/{customerId}")
    public ResponseEntity<?> getDueWhatsAppLink(@PathVariable Long customerId) {
        return customerRepo.findById(customerId)
                .map(customer -> {
                    if (customer.getPhone() == null || customer.getPhone().isBlank()) {
                        return ResponseEntity.badRequest().body("Customer's phone number is missing.");
                    }
                    String msg = whatsappService.generateDueReminderMessage(customer);
                    String link = whatsappService.getWhatsAppLink(customer.getPhone(), msg);
                    return ResponseEntity.ok(link);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/whatsapp/scheme/{customerId}")
    public ResponseEntity<?> getSchemeWhatsAppLink(@PathVariable Long customerId) {
        return customerRepo.findById(customerId)
                .map(customer -> {
                    if (customer.getPhone() == null || customer.getPhone().isBlank()) {
                        return ResponseEntity.badRequest().body("Customer's phone number is missing.");
                    }
                    String msg = whatsappService.generateSchemeReminderMessage(customer);
                    String link = whatsappService.getWhatsAppLink(customer.getPhone(), msg);
                    return ResponseEntity.ok(link);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/whatsapp/bill/{billId}")
    public ResponseEntity<?> getBillWhatsAppLink(@PathVariable Long billId) {
        return billRepo.findById(billId)
                .map(bill -> {
                    if (bill.getCustomer() == null) {
                        return ResponseEntity.badRequest().body("Customer is not associated with this bill.");
                    }
                    if (bill.getCustomer().getPhone() == null || bill.getCustomer().getPhone().isBlank()) {
                        return ResponseEntity.badRequest().body("Customer's phone number is missing.");
                    }
                    String msg = whatsappService.generateBillShareMessage(bill);
                    String link = whatsappService.getWhatsAppLink(bill.getCustomer().getPhone(), msg);
                    return ResponseEntity.ok(link);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
