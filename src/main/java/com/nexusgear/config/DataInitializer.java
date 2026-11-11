package com.nexusgear.config;

import com.nexusgear.model.mongo.Product;
import com.nexusgear.model.postgres.AdminUser;
import com.nexusgear.repository.mongo.ProductRepository;
import com.nexusgear.repository.postgres.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AdminUserRepository adminUserRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AdminUserRepository adminUserRepository,
                           ProductRepository productRepository,
                           PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedSupabasePostgresAdmin();
        seedMongoProducts();
    }

    private void seedSupabasePostgresAdmin() {
        try {
            String defaultEmail = "admin@nexusgear.com";
            if (adminUserRepository.findByEmailIgnoreCase(defaultEmail).isEmpty()) {
                AdminUser defaultAdmin = new AdminUser(
                        "Administrador Nexus",
                        defaultEmail,
                        passwordEncoder.encode("admin123"),
                        "ADMIN"
                );
                adminUserRepository.save(defaultAdmin);
            }
        } catch (Exception e) {
            log.warn("Supabase Postgres init warning: {}", e.getMessage());
        }
    }

    private void seedMongoProducts() {
        try {
            if (productRepository.count() == 0) {
                List<Product> initialProducts = List.of(
                        new Product(
                                "Monitor Gamer 27\" 4K 144Hz IPS",
                                "Monitor profissional para e-Sports com painel Fast IPS, resolução UHD 4K, 1ms de resposta e suporte a G-Sync/FreeSync Premium.",
                                "Monitores",
                                "ASUS ROG",
                                new BigDecimal("2999.99"),
                                new BigDecimal("3999.99"),
                                25,
                                "/images/monitor-asus.jpg",
                                4.9,
                                48,
                                Map.of("Resolucao", "3840x2160 (4K)", "TaxaAtualizacao", "144Hz", "Painel", "Fast IPS", "TempoResposta", "1ms")
                        ),
                        new Product(
                                "Processador AMD Ryzen 7 7800X3D",
                                "O processador definitivo para jogos com tecnologia AMD 3D V-Cache, 8 núcleos, 16 threads e boost de até 5.0GHz.",
                                "Processadores",
                                "AMD Ryzen",
                                new BigDecimal("2699.99"),
                                new BigDecimal("3299.99"),
                                18,
                                "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?auto=format&fit=crop&w=800&q=80",
                                5.0,
                                120,
                                Map.of("Socket", "AM5", "Nucleos", "8 Cores / 16 Threads", "CacheL3", "96MB 3D V-Cache", "TDP", "120W")
                        ),
                        new Product(
                                "Placa-Mãe ASUS ROG Z790 DDR5",
                                "Placa-mãe high-end com VRM de 20+1 fases, suporte a memória DDR5 até 7800MHz, PCIe 5.0 e Wi-Fi 6E integrado.",
                                "Placas-Mãe",
                                "ASUS ROG",
                                new BigDecimal("2729.99"),
                                new BigDecimal("3899.99"),
                                30,
                                "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
                                4.8,
                                35,
                                Map.of("Chipset", "Intel Z790", "Memoria", "DDR5 Dual Channel", "PCIe", "5.0 x16", "Conectividade", "Wi-Fi 6E + 2.5G LAN")
                        ),
                        new Product(
                                "Mouse Gamer RGB 26000 DPI Wireless",
                                "Mouse ultraleve sem fio com sensor óptico de 26.000 DPI, switches ópticos de 90M de cliques e iluminação RGB Chroma.",
                                "Mouses",
                                "Razer",
                                new BigDecimal("509.99"),
                                new BigDecimal("599.99"),
                                15,
                                "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=800&q=80",
                                4.9,
                                92,
                                Map.of("Sensor", "Focus Pro 26K DPI", "Conexao", "HyperSpeed Wireless / USB-C", "Peso", "63g")
                        ),
                        new Product(
                                "Teclado Mecânico Custom Switch Red",
                                "Teclado mecânico compacto 75% com estrutura Gasket Mount, switches lineares lubrificados de fábrica e keycaps PBT Double-Shot.",
                                "Teclados",
                                "Corsair",
                                new BigDecimal("719.99"),
                                new BigDecimal("899.99"),
                                20,
                                "https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=800&q=80",
                                4.9,
                                210,
                                Map.of("Switch", "Linear Red Custom", "Formato", "75% Hot-Swappable", "Keycaps", "PBT Double-Shot")
                        ),
                        new Product(
                                "Headset Gamer 7.1 Surround Wireless",
                                "Headset sem fio com áudio espacial THX 7.1 Surround, drivers de titânio de 50mm, microfone cardioide destacável e bateria de 50h.",
                                "Headsets",
                                "Razer",
                                new BigDecimal("599.99"),
                                new BigDecimal("799.99"),
                                25,
                                "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=800&q=80",
                                4.8,
                                64,
                                Map.of("Audio", "7.1 Surround Espacial", "Drivers", "50mm Titânio", "Bateria", "Até 50 horas")
                        )
                );

                productRepository.saveAll(initialProducts);
            } else {
                List<Product> monitors = productRepository.findByNameContainingIgnoreCase("Monitor Gamer 27");
                for (Product m : monitors) {
                    if (m.getImageUrl() != null && m.getImageUrl().contains("1527443224154")) {
                        m.setImageUrl("/images/monitor-asus.jpg");
                        productRepository.save(m);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("MongoDB init warning: {}", e.getMessage());
        }
    }
}
