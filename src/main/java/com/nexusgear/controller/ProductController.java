package com.nexusgear.controller;

import com.nexusgear.model.mongo.Product;
import com.nexusgear.service.ProductCatalogService;
import com.nexusgear.service.SupabaseAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductCatalogService productCatalogService;
    private final SupabaseAuthService supabaseAuthService;

    public ProductController(ProductCatalogService productCatalogService,
                             SupabaseAuthService supabaseAuthService) {
        this.productCatalogService = productCatalogService;
        this.supabaseAuthService = supabaseAuthService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productCatalogService.listAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        return productCatalogService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Produto não encontrado no MongoDB.")));
    }

    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Product product) {

        if (!supabaseAuthService.isValidSession(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Acesso negado. Faça login como Administrador no Supabase."));
        }

        if (product.getName() == null || product.getName().isBlank() || product.getPrice() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Nome e preço do produto são obrigatórios."));
        }

        Product created = productCatalogService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String id) {

        if (!supabaseAuthService.isValidSession(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Acesso negado. Faça login como Administrador no Supabase."));
        }

        productCatalogService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Produto removido do MongoDB com sucesso."));
    }
}
