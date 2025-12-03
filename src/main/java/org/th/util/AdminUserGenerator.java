package org.th.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Generate admin user SQL
 * Run this to get SQL for creating admin user
 */
public class AdminUserGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String encoded = encoder.encode(password);

        System.out.println("=== INSERT THIS SQL INTO YOUR DATABASE ===\n");
        System.out.println("INSERT INTO users (username, email, password, role, is_active, created_at)");
        System.out.println("VALUES ('admin', 'admin@mytogether.com', ");
        System.out.println("        '" + encoded + "',");
        System.out.println("        'ADMIN', true, NOW());");
        System.out.println("\n=== CREDENTIALS ===");
        System.out.println("Username: admin");
        System.out.println("Password: admin123");
        System.out.println("\n=== THEN USE IN SWAGGER ===");
        System.out.println("1. POST /api/auth/login");
        System.out.println("2. Copy JWT token from response");
        System.out.println("3. Click 'Authorize' in Swagger");
        System.out.println("4. Paste: Bearer {token}");
    }
}
