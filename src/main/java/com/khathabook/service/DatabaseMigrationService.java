package com.khathabook.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseMigrationService implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔧 [MIGRATION] Checking for legacy unique constraints...");
        try {
            // Index name from the error message: UK_qfr8vf85k3q1xinifvsl1eynf
            jdbcTemplate.execute("ALTER TABLE products DROP INDEX UK_qfr8vf85k3q1xinifvsl1eynf");
            System.out.println("✅ [MIGRATION] Successfully dropped legacy unique index UK_qfr8vf85k3q1xinifvsl1eynf");
        } catch (Exception e) {
            // Likely already dropped or doesn't exist
            if (e.getMessage() != null && e.getMessage().contains("check that it exists")) {
                 System.out.println("ℹ️ [MIGRATION] Legacy index UK_qfr8vf85k3q1xinifvsl1eynf not found. Skipping.");
            } else {
                 System.err.println("⚠️ [MIGRATION] Failed to drop index: " + e.getMessage());
            }
        }

        try {
            jdbcTemplate.execute("ALTER TABLE bills MODIFY COLUMN items TEXT");
            System.out.println("✅ [MIGRATION] Successfully modified bills.items column to TEXT");
        } catch (Exception e) {
            System.err.println("⚠️ [MIGRATION] Failed to modify bills.items column: " + e.getMessage());
        }
    }
}
