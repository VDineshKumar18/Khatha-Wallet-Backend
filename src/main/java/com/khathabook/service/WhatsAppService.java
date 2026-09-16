package com.khathabook.service;

import com.khathabook.model.Customer;
import com.khathabook.model.Order;
import com.khathabook.model.Bill;
import com.khathabook.model.Retailer;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class WhatsAppService {

    public String generateOrderUpdateMessage(Order order) {
        Customer customer = order.getCustomer();
        Retailer retailer = order.getRetailer();
        String status = order.getStatus();
        
        String emoji = switch (status) {
            case "ACCEPTED" -> "✅";
            case "PACKING" -> "📦";
            case "PACKED" -> "📦";
            case "READY_FOR_PICKUP" -> "🔔";
            case "COMPLETED" -> "🎉";
            default -> "ℹ️";
        };

        return String.format(
            "Hello %s, your order #%d is now %s! %s\n\nStatus: %s\nTotal: ₹ %.2f\n\nThank you for shopping with %s!",
            customer.getName(),
            order.getId(),
            status.toLowerCase().replace("_", " "),
            emoji,
            status,
            order.getTotalAmount(),
            retailer.getName()
        );
    }

    public String generateDueReminderMessage(Customer customer) {
        return String.format(
            "Hello %s, this is a friendly reminder from %s about your pending due amount of ₹ %.2f. Please clear it at your convenience.\n\nThank you!",
            customer.getName(),
            customer.getRetailer().getName(),
            customer.getDueAmount()
        );
    }

    public String generateSchemeReminderMessage(Customer customer) {
        return String.format(
            "Hello %s, this is a friendly reminder from %s regarding your Monthly Savings Scheme.\n\nTotal Saved: ₹ %.2f\nMonthly Deposit: ₹ %.2f\nTarget: ₹ %.2f\n\nPlease visit us to deposit your installment.\n\nThank you!",
            customer.getName(),
            customer.getRetailer().getName(),
            customer.getSchemeCollectedAmount(),
            customer.getSchemeMonthlyAmount() > 0 ? customer.getSchemeMonthlyAmount() : 500.0,
            customer.getSchemeTargetAmount() > 0 ? customer.getSchemeTargetAmount() : 6000.0
        );
    }

    public String generateBillShareMessage(Bill bill) {
        return String.format(
            "Hello %s, here are your bill details from %s:\n\nBill No: %s\nDate: %s\nAmount: ₹ %.2f\nPaid: ₹ %.2f\nDue: ₹ %.2f\n\nThank you!",
            bill.getCustomer().getName(),
            bill.getRetailer().getName(),
            bill.getBillNumber(),
            bill.getBillDate(),
            bill.getAmount(),
            bill.getPaidAmount(),
            bill.getDueAmount()
        );
    }

    public String getWhatsAppLink(String phone, String message) {
        if (phone == null || phone.isBlank()) return null;
        
        // Remove non-numeric characters
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        
        // Add country code if missing (assumes India +91 as default for this project context)
        if (cleanPhone.length() == 10) {
            cleanPhone = "91" + cleanPhone;
        }

        try {
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
            return "https://wa.me/" + cleanPhone + "?text=" + encodedMessage;
        } catch (Exception e) {
            return "https://wa.me/" + cleanPhone;
        }
    }
}
