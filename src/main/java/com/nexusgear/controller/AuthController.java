package com.nexusgear.controller;

import com.nexusgear.model.postgres.AdminUser;
import com.nexusgear.service.SupabaseAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SupabaseAuthService supabaseAuthService;

    public AuthController(SupabaseAuthService supabaseAuthService) {
        this.supabaseAuthService = supabaseAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        Optional<Map<String, Object>> session = supabaseAuthService.authenticate(email, password);
        if (session.isPresent()) {
            return ResponseEntity.ok(session.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "E-mail ou senha inválidos no Supabase (PostgreSQL)."));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        try {
            String name = payload.get("name");
            String email = payload.get("email");
            String password = payload.get("password");

            if (email == null || email.isBlank() || password == null || password.length() < 4) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Informe um e-mail válido e senha de no mínimo 4 caracteres."));
            }

            AdminUser created = supabaseAuthService.registerAdmin(name, email, password);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Administrador cadastrado com sucesso no Supabase (PostgreSQL)!",
                    "email", created.getEmail(),
                    "name", created.getName()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String token) {
        return supabaseAuthService.getAdminByToken(token)
                .<ResponseEntity<?>>map(admin -> ResponseEntity.ok(Map.of(
                        "name", admin.getName(),
                        "email", admin.getEmail(),
                        "role", admin.getRole()
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Sessão expirada ou não autenticada.")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        supabaseAuthService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso."));
    }
}
