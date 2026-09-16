package com.khathabook;

/**
 * Khatha Wallet Backend - Final Stability Version
 * Build Date: 2026-02-26
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableScheduling
public class KhathaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(KhathaBackendApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "Khatha Wallet Backend is Alive and Healthy! 🚀 (v2.2.6-stable)";
    }
}
