package com.nexusgear.service;

import com.nexusgear.model.postgres.AdminUser;
import com.nexusgear.repository.postgres.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SupabaseAuthService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseAuthService.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<String, AdminUser> activeSessions = new ConcurrentHashMap<>();

    @Value("${supabase.api.url:}")
    private String supabaseApiUrl;

    @Value("${supabase.api.anon-key:}")
    private String supabaseAnonKey;

    public SupabaseAuthService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Map<String, Object>> authenticate(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }

        String normalizedEmail = email.trim().toLowerCase();

        Optional<AdminUser> adminOpt = adminUserRepository.findByEmailIgnoreCase(normalizedEmail);
        if (adminOpt.isPresent()) {
            AdminUser admin = adminOpt.get();
            if (passwordEncoder.matches(password, admin.getPasswordHash())) {
                String token = UUID.randomUUID().toString();
                activeSessions.put(token, admin);
                log.info("[SUPABASE-POSTGRES] Login de administrador aprovado: {}", normalizedEmail);
                return Optional.of(Map.of(
                        "token", token,
                        "name", admin.getName(),
                        "email", admin.getEmail(),
                        "role", admin.getRole(),
                        "provider", "Supabase PostgreSQL"
                ));
            }
        }

        if (supabaseApiUrl != null && !supabaseApiUrl.isBlank()
                && supabaseAnonKey != null && !supabaseAnonKey.isBlank()) {
            try {
                String authEndpoint = supabaseApiUrl.replaceAll("/+$", "") + "/auth/v1/token?grant_type=password";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("apikey", supabaseAnonKey);

                HttpEntity<Map<String, String>> request = new HttpEntity<>(
                        Map.of("email", normalizedEmail, "password", password),
                        headers
                );

                ResponseEntity<Map> response = restTemplate.postForEntity(authEndpoint, request, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    AdminUser syncedAdmin = adminOpt.orElseGet(() -> {
                        AdminUser newAdmin = new AdminUser(
                                normalizedEmail.split("@")[0],
                                normalizedEmail,
                                passwordEncoder.encode(password),
                                "ADMIN"
                        );
                        return adminUserRepository.save(newAdmin);
                    });

                    String token = UUID.randomUUID().toString();
                    activeSessions.put(token, syncedAdmin);
                    return Optional.of(Map.of(
                            "token", token,
                            "name", syncedAdmin.getName(),
                            "email", syncedAdmin.getEmail(),
                            "role", syncedAdmin.getRole(),
                            "provider", "Supabase Auth + PostgreSQL"
                    ));
                }
            } catch (Exception e) {
                log.debug("Supabase Auth REST fallback: {}", e.getMessage());
            }
        }

        return Optional.empty();
    }

    public AdminUser registerAdmin(String name, String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        if (adminUserRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Já existe um administrador cadastrado com este e-mail no Supabase.");
        }
        AdminUser admin = new AdminUser(
                name != null && !name.isBlank() ? name.trim() : "Administrador Nexus",
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                "ADMIN"
        );
        return adminUserRepository.save(admin);
    }

    public boolean isValidSession(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String cleanToken = token.replace("Bearer ", "").trim();
        return activeSessions.containsKey(cleanToken);
    }

    public Optional<AdminUser> getAdminByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String cleanToken = token.replace("Bearer ", "").trim();
        return Optional.ofNullable(activeSessions.get(cleanToken));
    }

    public void logout(String token) {
        if (token != null) {
            activeSessions.remove(token.replace("Bearer ", "").trim());
        }
    }
}
