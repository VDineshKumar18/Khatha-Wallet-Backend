package com.khathabook.controller;

import com.khathabook.dto.ChatMessageDTO;
import com.khathabook.dto.OrderDTO;
import com.khathabook.model.ChatMessage;
import com.khathabook.model.Customer;
import com.khathabook.model.Order;
import com.khathabook.model.Retailer;
import com.khathabook.repository.ChatMessageRepository;
import com.khathabook.repository.CustomerRepository;
import com.khathabook.repository.OrderRepository;
import com.khathabook.repository.RetailerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RetailerRepository retailerRepository;

    @Autowired
    private com.khathabook.repository.BillRepository billRepository;

    @Autowired
    private com.khathabook.service.NotificationService notificationService; // ✅ Inject NotificationService

    @Autowired
    private com.khathabook.service.BillService billService; // ✅ Inject BillService for stock reduction

    @Autowired
    private ChatMessageRepository chatMessageRepository; // ✅ Inject ChatMessageRepository

    @Autowired
    private com.khathabook.repository.GatewayTransactionRepository gatewayTransactionRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO orderDTO) {
        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Retailer retailer = retailerRepository.findById(orderDTO.getRetailerId())
                .orElseThrow(() -> new RuntimeException("Retailer not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setRetailer(retailer);
        order.setItems(orderDTO.getItems());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setPaymentMode(orderDTO.getPaymentMode());
        order.setGatewayTransactionRef(orderDTO.getGatewayTransactionRef());
        order.setStatus("PLACED"); // State 1: Just created, stock not touched

        orderRepository.save(order);

        // ✅ Notify Retailer
        notificationService.sendNewOrderEmail(order, retailer);

        return ResponseEntity.ok("Order placed successfully");
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
        return ResponseEntity.ok(orders.stream().map(o -> convertToDTO(o, "CUSTOMER")).collect(Collectors.toList()));
    }

    @GetMapping("/retailer/{retailerId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByRetailer(@PathVariable Long retailerId) {
        List<Order> orders = orderRepository.findByRetailerIdOrderByOrderDateDesc(retailerId);
        return ResponseEntity.ok(orders.stream().map(o -> convertToDTO(o, "RETAILER")).collect(Collectors.toList()));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long orderId, 
            @RequestParam String status,
            @RequestParam(required = false) String otp,
            @RequestParam(required = false) Integer expectedMinutes
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        String oldStatus = order.getStatus();

        // VALIDATE STATE TRANSITIONS
        if ("ACCEPTED".equalsIgnoreCase(status)) {
            if (!"PLACED".equalsIgnoreCase(oldStatus) && !"MODIFICATION_REQUESTED".equalsIgnoreCase(oldStatus)) {
                return ResponseEntity.badRequest().body("Only PLACED or MODIFICATION_REQUESTED orders can be ACCEPTED");
            }
            order.setAcceptedAt(java.time.LocalDateTime.now());
            reduceStock(order);
            status = "STOCK_RESERVED"; // Automatically moves to reserved
        }
        else if ("REJECTED".equalsIgnoreCase(status)) {
            if (!"PLACED".equalsIgnoreCase(oldStatus) && !"MODIFICATION_REQUESTED".equalsIgnoreCase(oldStatus)) {
                return ResponseEntity.badRequest().body("Only PLACED or MODIFICATION_REQUESTED orders can be REJECTED");
            }
            // No stock to restore
        }
        else if ("PACKING".equalsIgnoreCase(status)) {
            if (!"STOCK_RESERVED".equalsIgnoreCase(oldStatus)) {
                return ResponseEntity.badRequest().body("Only STOCK_RESERVED orders can begin PACKING");
            }
            if (expectedMinutes != null) {
                order.setExpectedPackingTime(java.time.LocalDateTime.now().plusMinutes(expectedMinutes));
            }
        }
        else if ("PACKED".equalsIgnoreCase(status)) {
            if (!"PACKING".equalsIgnoreCase(oldStatus)) {
                return ResponseEntity.badRequest().body("Only PACKING orders can be marked as PACKED");
            }
            order.setPackedAt(java.time.LocalDateTime.now());
            if (order.getDeliveryOtp() == null) {
                String generatedOtp = String.format("%06d", new java.util.Random().nextInt(999999));
                order.setDeliveryOtp(generatedOtp);
            }
        }
        else if ("READY_FOR_PICKUP".equalsIgnoreCase(status)) {
            if (!"PACKED".equalsIgnoreCase(oldStatus)) {
                return ResponseEntity.badRequest().body("Only PACKED orders can be marked READY_FOR_PICKUP");
            }
            order.setReadyForPickupAt(java.time.LocalDateTime.now());
            if (order.getDeliveryOtp() == null) {
                String generatedOtp = String.format("%06d", new java.util.Random().nextInt(999999));
                order.setDeliveryOtp(generatedOtp);
            }
        }
        else if ("COMPLETED".equalsIgnoreCase(status) || "DELIVERED".equalsIgnoreCase(status)) {
            if (!"READY_FOR_PICKUP".equalsIgnoreCase(oldStatus)) {
                return ResponseEntity.badRequest().body("Only READY_FOR_PICKUP orders can be COMPLETED");
            }
            if (order.getDeliveryOtp() != null && !order.getDeliveryOtp().equals(otp)) {
                return ResponseEntity.badRequest().body("Invalid Delivery OTP");
            }
            status = "COMPLETED";
            generateBillForOrder(order);
        }
        else if ("CANCELLED".equalsIgnoreCase(status)) {
            if ("PACKING".equalsIgnoreCase(oldStatus) || "PACKED".equalsIgnoreCase(oldStatus)) {
                if (order.getExpectedPackingTime() != null && java.time.LocalDateTime.now().isBefore(order.getExpectedPackingTime())) {
                    return ResponseEntity.badRequest().body("Order is being packed. Cannot cancel until expected time has passed.");
                }
            } else if ("READY_FOR_PICKUP".equalsIgnoreCase(oldStatus) || "COMPLETED".equalsIgnoreCase(oldStatus)) {
                 return ResponseEntity.badRequest().body("Cannot cancel at this stage.");
            }
            
            // Restore stock if it was already deducted
            if (isStockDeducted(oldStatus)) {
                restoreStock(order);
            }

            // Automate Refund if prepaid via Payment Gateway
            if (order.getGatewayTransactionRef() != null && !order.getGatewayTransactionRef().trim().isEmpty()) {
                gatewayTransactionRepository.findByTransactionRef(order.getGatewayTransactionRef())
                    .ifPresent(txn -> {
                        txn.setStatus("REFUNDED");
                        gatewayTransactionRepository.save(txn);
                        System.out.println("💳 [REFUND] Successfully refunded transaction: " + txn.getTransactionRef());
                    });
            }
        }
        else if ("MODIFICATION_REQUESTED".equalsIgnoreCase(status)) {
             if (isStockDeducted(oldStatus)) {
                 restoreStock(order);
             }
        }

        order.setStatus(status);
        orderRepository.save(order);

        // Notify Customer if status changes
        if (!status.equals(oldStatus)) {
            notificationService.sendOrderStatusEmail(order, order.getCustomer());
        }

        return ResponseEntity.ok("Status updated");
    }

    @PutMapping("/{orderId}/modify")
    public ResponseEntity<?> modifyOrder(
            @PathVariable Long orderId,
            @RequestBody OrderDTO updatedDTO
    ) {
         Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
         
         if (isStockDeducted(order.getStatus())) {
             restoreStock(order); // Return old stock
         }
         
         order.setItems(updatedDTO.getItems());
         order.setTotalAmount(updatedDTO.getTotalAmount());
         order.setStatus("MODIFICATION_REQUESTED");
         orderRepository.save(order);
         
         notificationService.sendOrderStatusEmail(order, order.getCustomer());
         return ResponseEntity.ok("Order modified successfully");
    }

    private boolean isStockDeducted(String status) {
        return "STOCK_RESERVED".equalsIgnoreCase(status) || 
               "PACKING".equalsIgnoreCase(status) || 
               "PACKED".equalsIgnoreCase(status) || 
               "READY_FOR_PICKUP".equalsIgnoreCase(status);
    }

    private void reduceStock(Order order) {
        try {
            String itemsJson = order.getItems();
            String formattedItems = "";
            
            if (itemsJson != null && !itemsJson.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(itemsJson);
                
                StringBuilder sb = new StringBuilder();
                if (rootNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
                        String barcode = node.has("barcode") ? node.get("barcode").asText() : "";
                        double qty = node.has("qty") ? node.get("qty").asDouble() : 0;
                        
                        if (!barcode.isEmpty() && qty > 0) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(barcode).append(" x ").append(qty);
                        }
                    }
                }
                formattedItems = sb.toString();
            }

            if (!formattedItems.isEmpty()) {
                 billService.reduceStockFromBill(formattedItems, order.getRetailer().getId());
            }
        } catch (Exception e) {
             System.err.println("Stock Reduction Failed: " + e.getMessage());
        }
    }

    private void restoreStock(Order order) {
        try {
            billService.restoreStock(order.getItems(), order.getRetailer().getId());
        } catch (Exception e) {
            System.err.println("Stock Restoration Failed: " + e.getMessage());
        }
    }

    private void generateBillForOrder(Order order) {
        com.khathabook.model.Bill bill = new com.khathabook.model.Bill();
        bill.setRetailer(order.getRetailer());
        bill.setCustomer(order.getCustomer());
        bill.setBillDate(java.time.LocalDateTime.now());
        bill.setItems(order.getItems());
        bill.setAmount(order.getTotalAmount());
        bill.setBillNumber("ORD-" + order.getId());
        bill.setType("SALE");
        bill.setPaymentMode(order.getPaymentMode());

        if ("KHATHA".equalsIgnoreCase(order.getPaymentMode())) {
            bill.setStatus("DUE"); // Unpaid
            bill.setPaid(false);
            bill.setPaidAmount(0);
            bill.setDueAmount(order.getTotalAmount());

            Customer customer = order.getCustomer();
            customer.setDueAmount(customer.getDueAmount() + order.getTotalAmount());
            customerRepository.save(customer);

        } else {
            bill.setStatus("PAID");
            bill.setPaid(true);
            bill.setPaidAmount(order.getTotalAmount());
            bill.setDueAmount(0);
            
             Customer customer = order.getCustomer();
             customer.setTotalReceived(customer.getTotalReceived() + order.getTotalAmount());
             customerRepository.save(customer);
        }

        billRepository.save(bill);
    }

    private OrderDTO convertToDTO(Order order, String role) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer().getName()); // ✅ POPULATE
        dto.setCustomerPhone(order.getCustomer().getPhone()); // ✅ POPULATE
        dto.setRetailerId(order.getRetailer().getId());
        dto.setRetailerName(order.getRetailer().getName()); // ✅ POPULATE NAME
        dto.setItems(order.getItems());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMode(order.getPaymentMode());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate() != null ? order.getOrderDate().toString() : null);
        dto.setAcceptedAt(order.getAcceptedAt() != null ? order.getAcceptedAt().toString() : null);
        dto.setExpectedPackingTime(order.getExpectedPackingTime() != null ? order.getExpectedPackingTime().toString() : null);
        dto.setReadyForPickupAt(order.getReadyForPickupAt() != null ? order.getReadyForPickupAt().toString() : null);
        dto.setPackedAt(order.getPackedAt() != null ? order.getPackedAt().toString() : null);
        dto.setGatewayTransactionRef(order.getGatewayTransactionRef());
        
        Integer unreadCount = 0;
        if (role != null) {
            unreadCount = chatMessageRepository.countByOrderIdAndSenderRoleNotAndIsReadFalse(order.getId(), role);
        }
        dto.setUnreadChatCount(unreadCount != null ? unreadCount : 0);
        
        return dto;
    }

    // ==========================================
    // =              ORDER CHAT APIs           =
    // ==========================================

    @GetMapping("/{orderId}/chat")
    public ResponseEntity<List<ChatMessageDTO>> getOrderChat(@PathVariable Long orderId) {
        List<ChatMessage> messages = chatMessageRepository.findByOrderIdOrderByTimestampAsc(orderId);
        return ResponseEntity.ok(messages.stream().map(this::convertChatToDTO).collect(Collectors.toList()));
    }

    @PutMapping("/{orderId}/chat/read")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable Long orderId, @RequestParam String role) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findByOrderIdAndSenderRoleNotAndIsReadFalse(orderId, role);
        for (ChatMessage msg : unreadMessages) {
            msg.setRead(true);
        }
        if (!unreadMessages.isEmpty()) {
            chatMessageRepository.saveAll(unreadMessages);
        }
        return ResponseEntity.ok("Messages marked as read");
    }

    @PostMapping("/{orderId}/chat")
    public ResponseEntity<?> sendChatMessage(
            @PathVariable Long orderId,
            @RequestBody ChatMessageDTO messageDTO) {
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        ChatMessage message = new ChatMessage();
        message.setOrder(order);
        message.setSenderId(messageDTO.getSenderId());
        message.setSenderRole(messageDTO.getSenderRole());
        message.setMessage(messageDTO.getMessage());
        
        chatMessageRepository.save(message);

        // Optional: Send a notification to the other party here in the future
        
        return ResponseEntity.ok(convertChatToDTO(message));
    }

    private ChatMessageDTO convertChatToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setOrderId(message.getOrder().getId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderRole(message.getSenderRole());
        dto.setMessage(message.getMessage());
        dto.setRead(message.isRead());
        dto.setTimestamp(message.getTimestamp() != null ? message.getTimestamp().toString() : null);
        return dto;
    }
}
