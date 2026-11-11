package com.nexusgear.service;

import com.nexusgear.model.mongo.Product;
import com.nexusgear.repository.mongo.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductCatalogService {

    private static final Logger log = LoggerFactory.getLogger(ProductCatalogService.class);

    private final ProductRepository productRepository;

    public ProductCatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> listAllProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        product.setCreatedAt(LocalDateTime.now());
        if (product.getRating() == null) {
            product.setRating(5.0);
        }
        if (product.getReviewsCount() == null) {
            product.setReviewsCount(1);
        }

        if (product.getDiscountPercent() == null
                && product.getOldPrice() != null
                && product.getPrice() != null
                && product.getOldPrice().compareTo(product.getPrice()) > 0) {
            BigDecimal diff = product.getOldPrice().subtract(product.getPrice());
            int percent = diff.multiply(BigDecimal.valueOf(100))
                    .divide(product.getOldPrice(), 0, RoundingMode.HALF_UP)
                    .intValue();
            product.setDiscountPercent(percent);
        }

        Product saved = productRepository.save(product);
        log.info("[MONGODB] Novo produto cadastrado com sucesso no documento id={} ({})", saved.getId(), saved.getName());
        return saved;
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
        log.info("[MONGODB] Produto removido do catálogo id={}", id);
    }
}
