package com.nexusgear.controller;

import com.nexusgear.model.postgres.OrderLog;
import com.nexusgear.repository.postgres.OrderLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final OrderLogRepository orderLogRepository;

    @Value("${nexus.whatsapp.phone:5511999999999}")
    private String whatsappPhone;

    public CheckoutController(OrderLogRepository orderLogRepository) {
        this.orderLogRepository = orderLogRepository;
    }

    public static class CartItemRequest {
        private String productId;
        private String name;
        private BigDecimal price;
        private Integer quantity;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class CheckoutRequest {
        private List<CartItemRequest> items = new ArrayList<>();
        private String customPhone;

        public List<CartItemRequest> getItems() { return items; }
        public void setItems(List<CartItemRequest> items) { this.items = items; }
        public String getCustomPhone() { return customPhone; }
        public void setCustomPhone(String customPhone) { this.customPhone = customPhone; }
    }

    @PostMapping("/whatsapp")
    public ResponseEntity<?> generateWhatsappOrder(@RequestBody CheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "O carrinho está vazio."));
        }

        List<String> formattedItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItemRequest item : request.getItems()) {
            int qty = item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1;
            BigDecimal unitPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(qty)));
            formattedItems.add(qty + "x " + item.getName());
        }

        NumberFormat brFormat = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        brFormat.setMinimumFractionDigits(2);
        brFormat.setMaximumFractionDigits(2);
        String formattedTotal = brFormat.format(total);

        String itemsSummary = String.join(", ", formattedItems);
        String message = "Olá, gostaria de pedir: " + itemsSummary + ". Total: R$ " + formattedTotal;

        String phone = (request.getCustomPhone() != null && !request.getCustomPhone().isBlank())
                ? request.getCustomPhone().replaceAll("\\D", "")
                : whatsappPhone.replaceAll("\\D", "");

        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String whatsappUrl = "https://wa.me/" + phone + "?text=" + encodedMessage;

        try {
            OrderLog savedLog = orderLogRepository.save(new OrderLog(itemsSummary, total, whatsappUrl));
            log.info("[SUPABASE-POSTGRES] Pedido #{} registrado com sucesso. Total: R$ {}", savedLog.getId(), formattedTotal);
        } catch (Exception e) {
            log.warn("Não foi possível salvar log de pedido no PostgreSQL: {}", e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "message", message,
                "total", total,
                "formattedTotal", "R$ " + formattedTotal,
                "whatsappUrl", whatsappUrl
        ));
    }

    @GetMapping("/orders")
    public List<OrderLog> listRecentOrders() {
        return orderLogRepository.findTop20ByOrderByCreatedAtDesc();
    }
}
