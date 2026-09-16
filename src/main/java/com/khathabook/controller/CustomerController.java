package com.khathabook.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.khathabook.model.Customer;
import com.khathabook.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public List<Customer> getCustomers(
            @RequestHeader("X-Retailer-Id") Long retailerId
    ) {
        return service.getAllByRetailer(retailerId);
    }

    // ✅ NEW: GET SINGLE CUSTOMER (FOR PROFILE)
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public Customer addCustomer(
            @RequestHeader("X-Retailer-Id") Long retailerId,
            @RequestBody Customer customer
    ) {
        return service.save(customer, retailerId);
    }


    @PutMapping("/{id}/email")
    public Customer updateEmail(
            @PathVariable Long id,
            @RequestParam String email
    ) {
        Customer customer = service.getById(id);
        customer.setEmail(email);
        return service.save(customer);
    }

    @PutMapping("/{id}/phone")
    public Customer updatePhone(
            @PathVariable Long id,
            @RequestParam String phone
    ) {
        Customer customer = service.getById(id);
        customer.setPhone(phone);
        return service.save(customer);
    }

    // ✅ NEW: UPDATE SCHEME DETAILS
    @PutMapping("/{id}/scheme")
    public Customer updateScheme(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> updates
    ) {
        Customer customer = service.getById(id);

        if (updates.containsKey("isSchemeActive")) {
            customer.setIsSchemeActive((Boolean) updates.get("isSchemeActive"));
            if (customer.getIsSchemeActive() && customer.getSchemeStartDate() == null) {
                customer.setSchemeStartDate(java.time.LocalDate.now());
            }
        }
        if (updates.containsKey("schemeMonthlyAmount")) {
            customer.setSchemeMonthlyAmount(((Number) updates.get("schemeMonthlyAmount")).doubleValue());
        }
        if (updates.containsKey("schemeCollectedAmount")) {
            customer.setSchemeCollectedAmount(((Number) updates.get("schemeCollectedAmount")).doubleValue());
        }
        if (updates.containsKey("schemeTargetAmount")) {
            customer.setSchemeTargetAmount(((Number) updates.get("schemeTargetAmount")).doubleValue());
        }
        if (updates.containsKey("schemeMonthsPaid")) {
            customer.setSchemeMonthsPaid(((Number) updates.get("schemeMonthsPaid")).intValue());
        }

        return service.save(customer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(
            @PathVariable Long id,
            @RequestHeader("X-Retailer-Id") Long retailerId
    ) {
        try {
            service.deleteCustomer(id, retailerId);
            return ResponseEntity.ok("Customer deleted");
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
