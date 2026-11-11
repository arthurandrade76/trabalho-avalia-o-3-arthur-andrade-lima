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
import java.util.Set;
import java.util.stream.Collectors;

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
            List<Product> catalog = buildCatalogProducts();
            List<Product> existing = productRepository.findAll();

            for (Product p : existing) {
                if (p.getImageUrl() != null && p.getImageUrl().contains("1527443224154")) {
                    p.setImageUrl("/images/monitor-asus.jpg");
                    productRepository.save(p);
                }
            }

            Set<String> existingNames = existing.stream()
                    .map(p -> p.getName() != null ? p.getName().toLowerCase() : "")
                    .collect(Collectors.toSet());

            List<Product> missing = catalog.stream()
                    .filter(p -> !existingNames.contains(p.getName().toLowerCase()))
                    .toList();

            if (!missing.isEmpty()) {
                productRepository.saveAll(missing);
                log.info("[MONGODB] {} novos produtos adicionados ao catalogo (Total: {}).",
                        missing.size(), productRepository.count());
            }
        } catch (Exception e) {
            log.warn("MongoDB init warning: {}", e.getMessage());
        }
    }

    private List<Product> buildCatalogProducts() {
        return List.of(
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
                ),
                new Product(
                        "Teclado Mecânico ASUS ROG Azoth 75% OLED",
                        "Teclado mecânico sem fio com display OLED integrado, switches ROG NX pré-lubrificados, amortecimento em silicone de 3 camadas e conexão Tri-Mode.",
                        "Teclados",
                        "ASUS ROG",
                        new BigDecimal("1499.99"),
                        new BigDecimal("1899.99"),
                        21,
                        "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80",
                        5.0,
                        87,
                        Map.of("Switch", "ROG NX Red", "Display", "OLED 2 Polegadas", "Conexao", "2.4GHz / Bluetooth / USB-C")
                ),
                new Product(
                        "Teclado Mecânico Razer Huntsman V3 Pro Analog",
                        "Teclado competitivo com switches ópticos analógicos de 2ª geração, modo Rapid Trigger ajustável de 0.1mm a 4.0mm e apoio de pulso magnético.",
                        "Teclados",
                        "Razer",
                        new BigDecimal("1349.99"),
                        new BigDecimal("1699.99"),
                        20,
                        "https://images.unsplash.com/photo-1541140532154-b024d705b90a?auto=format&fit=crop&w=800&q=80",
                        4.9,
                        143,
                        Map.of("Switch", "Óptico Analógico Gen-2", "Recurso", "Rapid Trigger", "Estrutura", "Alumínio Escovado")
                ),
                new Product(
                        "Teclado Mecânico Corsair K70 Max RGB Magnético",
                        "Switches magnéticos-mecânicos Corsair MGX com ponto de atuação ajustável por tecla, polling rate de 8000Hz AXON e espuma acústica dupla.",
                        "Teclados",
                        "Corsair",
                        new BigDecimal("1099.99"),
                        new BigDecimal("1399.99"),
                        21,
                        "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?auto=format&fit=crop&w=800&q=80",
                        4.8,
                        76,
                        Map.of("Switch", "Corsair MGX Magnético", "PollingRate", "8000Hz Hyper-Polling", "Keycaps", "PBT Double-Shot")
                ),
                new Product(
                        "Teclado Mecânico Low Profile RGB 65% Wireless",
                        "Design ultracompacto 65% de perfil baixo com switches mecânicos táteis silenciosos, chassi em alumínio CNC e autonomia de até 200 horas.",
                        "Teclados",
                        "Corsair",
                        new BigDecimal("649.99"),
                        new BigDecimal("799.99"),
                        19,
                        "https://images.unsplash.com/photo-1563191911-e65f8655ebf9?auto=format&fit=crop&w=800&q=80",
                        4.7,
                        54,
                        Map.of("Formato", "65% Low Profile", "Bateria", "Até 200 horas", "Peso", "520g")
                ),
                new Product(
                        "Teclado Mecânico TKL Pro e-Sports Switch Speed",
                        "Layout Tenkeyless pensado para torneios, cabo USB-C destacável em malha trançada, iluminação RGB por tecla e memória onboard para 5 perfis.",
                        "Teclados",
                        "ASUS ROG",
                        new BigDecimal("579.99"),
                        new BigDecimal("749.99"),
                        23,
                        "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?auto=format&fit=crop&w=800&q=80",
                        4.8,
                        112,
                        Map.of("Switch", "Linear Speed Silver", "Formato", "TKL (80%)", "TaxaResposta", "1000Hz / 1ms")
                ),
                new Product(
                        "Monitor Gamer OLED 32\" 4K 240Hz G-Sync",
                        "Painel QD-OLED de 3ª geração com tempo de resposta instantâneo de 0.03ms, taxa de 240Hz em 4K nativo, DisplayHDR True Black 400 e dissipador customizado.",
                        "Monitores",
                        "ASUS ROG",
                        new BigDecimal("6499.99"),
                        new BigDecimal("7999.99"),
                        19,
                        "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=800&q=80",
                        5.0,
                        39,
                        Map.of("Painel", "QD-OLED 4K", "TaxaAtualizacao", "240Hz", "TempoResposta", "0.03ms (GtG)")
                ),
                new Product(
                        "Monitor Gamer Curvo 34\" WQHD 180Hz Ultrawide",
                        "Imersão total em proporção 21:9 com curvatura 1500R, resolução 3440x1440, 99% sRGB e tecnologia Extreme Low Motion Blur Sync.",
                        "Monitores",
                        "Corsair",
                        new BigDecimal("2599.99"),
                        new BigDecimal("3299.99"),
                        21,
                        "https://images.unsplash.com/photo-1593640408182-31c70c8268f5?auto=format&fit=crop&w=800&q=80",
                        4.8,
                        63,
                        Map.of("Resolucao", "3440x1440 Ultrawide", "TaxaAtualizacao", "180Hz", "Curvatura", "1500R")
                ),
                new Product(
                        "Monitor Gamer 24.5\" FHD 360Hz e-Sports",
                        "Desenvolvido para jogadores profissionais de FPS com painel Fast IPS de 360Hz, analisador de latência NVIDIA Reflex e base compacta.",
                        "Monitores",
                        "ASUS ROG",
                        new BigDecimal("2199.99"),
                        new BigDecimal("2799.99"),
                        21,
                        "/images/monitor-asus.jpg",
                        4.9,
                        81,
                        Map.of("Resolucao", "1920x1080 (FHD)", "TaxaAtualizacao", "360Hz", "TempoResposta", "0.5ms")
                ),
                new Product(
                        "Processador AMD Ryzen 9 7950X3D 16-Core",
                        "Topo de linha absoluto para criação e jogos extremos com 16 núcleos, 32 threads, 144MB de cache total e clock boost de até 5.7GHz.",
                        "Processadores",
                        "AMD Ryzen",
                        new BigDecimal("3899.99"),
                        new BigDecimal("4699.99"),
                        17,
                        "https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=800&q=80",
                        5.0,
                        95,
                        Map.of("Socket", "AM5", "Nucleos", "16 Cores / 32 Threads", "BoostClock", "Até 5.7GHz")
                ),
                new Product(
                        "Processador Intel Core i9-14900K 6.0GHz",
                        "Arquitetura híbrida de 24 núcleos (8 P-Cores + 16 E-Cores) e 32 threads, atingindo até 6.0GHz com Thermal Velocity Boost.",
                        "Processadores",
                        "Intel Core",
                        new BigDecimal("3499.99"),
                        new BigDecimal("4199.99"),
                        17,
                        "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?auto=format&fit=crop&w=800&q=80",
                        4.9,
                        108,
                        Map.of("Socket", "LGA1700", "Nucleos", "24 Cores / 32 Threads", "FrequenciaMax", "6.0GHz")
                ),
                new Product(
                        "Processador Intel Core i7-14700K 20-Core",
                        "Equilíbrio perfeito para jogos em 4K e streaming simultâneo com 20 núcleos, 28 threads, 33MB de Intel Smart Cache e suporte DDR5.",
                        "Processadores",
                        "Intel Core",
                        new BigDecimal("2499.99"),
                        new BigDecimal("2999.99"),
                        17,
                        "https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=800&q=80",
                        4.8,
                        74,
                        Map.of("Socket", "LGA1700", "Nucleos", "20 Cores / 28 Threads", "FrequenciaMax", "5.6GHz")
                ),
                new Product(
                        "Placa-Mãe ASUS ROG Crosshair X670E Hero",
                        "Construída para processadores AMD Ryzen série 7000/9000 com 18+2 estágios de potência, dois slots PCIe 5.0 x16 e cinco slots M.2.",
                        "Placas-Mãe",
                        "ASUS ROG",
                        new BigDecimal("3599.99"),
                        new BigDecimal("4499.99"),
                        20,
                        "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
                        4.9,
                        42,
                        Map.of("Socket", "AM5", "Chipset", "AMD X670E", "Memoria", "DDR5 até 8000MHz")
                ),
                new Product(
                        "Placa-Mãe B650M Elite AX Wi-Fi DDR5",
                        "Formato Micro-ATX robusto com dissipadores térmicos ampliados, slot M.2 PCIe 5.0, LAN 2.5GbE e Wi-Fi 6E integrado.",
                        "Placas-Mãe",
                        "AMD Ryzen",
                        new BigDecimal("1399.99"),
                        new BigDecimal("1799.99"),
                        22,
                        "https://images.unsplash.com/photo-1562976540-1502c2145186?auto=format&fit=crop&w=800&q=80",
                        4.8,
                        68,
                        Map.of("Socket", "AM5", "Chipset", "AMD B650", "Formato", "Micro-ATX")
                ),
                new Product(
                        "Placa-Mãe Z790 TUF Gaming Plus Wi-Fi D5",
                        "Componentes de nível militar TUF, VRM DrMOS de 16+1 fases, quatro slots M.2 PCIe 4.0 e iluminação Aura Sync RGB.",
                        "Placas-Mãe",
                        "Intel Core",
                        new BigDecimal("1899.99"),
                        new BigDecimal("2399.99"),
                        21,
                        "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
                        4.8,
                        51,
                        Map.of("Socket", "LGA1700", "Chipset", "Intel Z790", "Formato", "ATX")
                ),
                new Product(
                        "Mouse Gamer Razer Viper V3 Pro Ultralight 54g",
                        "Design simétrico ultraleve de 54g desenvolvido com profissionais de e-Sports, sensor óptico de 35.000 DPI e HyperPolling 8000Hz.",
                        "Mouses",
                        "Razer",
                        new BigDecimal("999.99"),
                        new BigDecimal("1249.99"),
                        20,
                        "https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=800&q=80",
                        5.0,
                        134,
                        Map.of("Peso", "54g Ultralight", "Sensor", "Focus Pro 35K", "PollingRate", "8000Hz Wireless")
                ),
                new Product(
                        "Mouse Gamer ASUS ROG Harpe Ace Aim Lab Edition",
                        "Criado em parceria com a Aim Lab com carcaça em bio-nylon de 54g, sensor ROG AimPoint de 36.000 DPI e baixa latência SpeedNova.",
                        "Mouses",
                        "ASUS ROG",
                        new BigDecimal("699.99"),
                        new BigDecimal("899.99"),
                        22,
                        "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=800&q=80",
                        4.9,
                        59,
                        Map.of("Peso", "54g", "Sensor", "ROG AimPoint 36K", "Bateria", "Até 90 horas")
                ),
                new Product(
                        "Mouse Gamer Corsair M75 Air Wireless",
                        "Formato ambidestro esculpido para máxima velocidade de flick, switches ópticos Quickstrike com zero delay e pés 100% PTFE virgem.",
                        "Mouses",
                        "Corsair",
                        new BigDecimal("549.99"),
                        new BigDecimal("699.99"),
                        21,
                        "https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=800&q=80",
                        4.8,
                        47,
                        Map.of("Peso", "60g", "Sensor", "Corsair Marksman 26K", "Conexao", "Slipstream Wireless / BT")
                ),
                new Product(
                        "Headset Gamer Razer BlackShark V2 Pro Wireless",
                        "Isolamento passivo avançado, microfone HyperClear Super Wideband de alta definição e perfis de áudio calibrados para FPS competitivo.",
                        "Headsets",
                        "Razer",
                        new BigDecimal("1199.99"),
                        new BigDecimal("1499.99"),
                        20,
                        "https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=800&q=80",
                        4.9,
                        115,
                        Map.of("Drivers", "TriForce Titanium 50mm", "Bateria", "Até 70 horas", "Conexao", "2.4GHz / Bluetooth 5.2")
                ),
                new Product(
                        "Headset Gamer Corsair Virtuoso RGB Wireless XT",
                        "Construção em alumínio usinado, áudio de alta fidelidade 24-bit/96kHz, suporte a Dolby Atmos e conexão simultânea Dual-Wireless.",
                        "Headsets",
                        "Corsair",
                        new BigDecimal("1299.99"),
                        new BigDecimal("1649.99"),
                        21,
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80",
                        4.8,
                        72,
                        Map.of("Audio", "Dolby Atmos Spatial", "Frequencia", "20Hz - 40.000Hz", "Estrutura", "Alumínio Premium")
                ),
                new Product(
                        "Headset Gamer ASUS ROG Delta S Wireless",
                        "Conchas ergonômicas em formato D, microfones AI Beamforming com cancelamento de ruído inteligente e drivers ASUS Essence de 50mm.",
                        "Headsets",
                        "ASUS ROG",
                        new BigDecimal("949.99"),
                        new BigDecimal("1199.99"),
                        21,
                        "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=800&q=80",
                        4.8,
                        53,
                        Map.of("Drivers", "ASUS Essence 50mm", "Microfone", "AI Beamforming", "Peso", "318g")
                )
        );
    }
}
