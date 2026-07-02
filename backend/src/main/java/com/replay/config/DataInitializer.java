package com.replay.config;

import com.replay.auth.Role;
import com.replay.auth.User;
import com.replay.auth.UserRepository;
import com.replay.product.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping DataInitializer");
            return;
        }

        log.info("Seeding database with demo data...");

        User admin = new User();
        admin.setEmail("admin@replay.fr");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFirstName("Admin");
        admin.setLastName("RePlay");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        User demoUser = new User();
        demoUser.setEmail("user@replay.fr");
        demoUser.setPassword(passwordEncoder.encode("user123"));
        demoUser.setFirstName("Russell");
        demoUser.setLastName("Demo");
        demoUser.setRole(Role.USER);
        userRepository.save(demoUser);

        List<Category> categories = List.of(
                createCategory("Consoles", "consoles", "Consoles de salon et portables"),
                createCategory("Jeux", "jeux", "Jeux video toutes consoles"),
                createCategory("Manettes", "manettes", "Manettes et controleurs"),
                createCategory("Accessoires", "accessoires", "Accessoires et peripheriques"),
                createCategory("Goodies", "goodies", "Produits derives et collection")
        );
        categoryRepository.saveAll(categories);

        Category consoles = categories.get(0);
        Category jeux = categories.get(1);
        Category manettes = categories.get(2);
        Category accessoires = categories.get(3);
        Category goodies = categories.get(4);

        List<Product> products = List.of(
                createProduct("NES Classic Edition", "nes-classic-edition", "La console NES mini avec 30 jeux pre-installes", new BigDecimal("59.99"), ProductCondition.RECONDITIONNE, ConsoleType.NES, 15, consoles, "https://upload.wikimedia.org/wikipedia/commons/8/82/Nintendo-Entertainment-System-NES-Console-FL.jpg"),
                createProduct("Super Nintendo SNES", "super-nintendo-snes", "Console SNES originale (pal)", new BigDecimal("89.99"), ProductCondition.BON_ETAT, ConsoleType.SNES, 8, consoles, "https://upload.wikimedia.org/wikipedia/commons/3/36/SNES-Mod1-Console-Set.png"),
                createProduct("Nintendo 64", "nintendo-64", "Console Nintendo 64 avec 1 manette", new BigDecimal("79.99"), ProductCondition.BON_ETAT, ConsoleType.NINTENDO_64, 5, consoles, "https://upload.wikimedia.org/wikipedia/commons/b/be/N64-Console-Set.png"),
                createProduct("GameCube", "gamecube", "Console GameCube violette", new BigDecimal("69.99"), ProductCondition.BON_ETAT, ConsoleType.GAMECUBE, 7, consoles, "https://upload.wikimedia.org/wikipedia/commons/4/46/GameCube-Console-Set.png"),
                createProduct("Wii", "wii", "Console Wii blanche avec capteur", new BigDecimal("49.99"), ProductCondition.RECONDITIONNE, ConsoleType.WII, 20, consoles, "https://upload.wikimedia.org/wikipedia/commons/f/f3/Wii-Console.png"),
                createProduct("PlayStation 2 Slim", "playstation-2-slim", "Console PS2 Slim noire avec 1 manette", new BigDecimal("54.99"), ProductCondition.BON_ETAT, ConsoleType.PS2, 12, consoles, "https://upload.wikimedia.org/wikipedia/commons/4/4f/PS2-Slim-Console-Set.png"),
                createProduct("PlayStation 3", "playstation-3", "Console PS3 120 Go slim", new BigDecimal("89.99"), ProductCondition.RECONDITIONNE, ConsoleType.PS3, 10, consoles, "https://upload.wikimedia.org/wikipedia/commons/5/5e/PS3-Consoles-Set.jpg"),
                createProduct("Game Boy Advance SP", "game-boy-advance-sp", "Console portable GBA SP argent", new BigDecimal("64.99"), ProductCondition.BON_ETAT, ConsoleType.GAMEBOY_ADVANCE, 6, consoles, "https://upload.wikimedia.org/wikipedia/commons/0/09/Game-Boy-Advance-SP-Mario-Left.png"),
                createProduct("Nintendo DS Lite", "nintendo-ds-lite", "Console Nintendo DS Lite rose", new BigDecimal("44.99"), ProductCondition.BON_ETAT, ConsoleType.NINTENDO_DS, 9, consoles, "https://upload.wikimedia.org/wikipedia/commons/4/4b/Nintendo-DS-Lite-Open.png"),

                createProduct("Super Mario World (SNES)", "super-mario-world-snes", "Le classique platformer de Nintendo", new BigDecimal("24.99"), ProductCondition.BON_ETAT, ConsoleType.SNES, 30, jeux, "https://upload.wikimedia.org/wikipedia/en/3/32/Super_Mario_World_Coverart.png"),
                createProduct("The Legend of Zelda Ocarina of Time", "zelda-ocarina-of-time", "L'aventure legendaire sur N64", new BigDecimal("34.99"), ProductCondition.BON_ETAT, ConsoleType.NINTENDO_64, 15, jeux, "https://upload.wikimedia.org/wikipedia/en/5/5d/The_Legend_of_Zelda-_Ocarina_of_Time_box_art.jpg"),
                createProduct("Mario Kart 64", "mario-kart-64", "Le jeu de course multijoueur par excellence", new BigDecimal("29.99"), ProductCondition.BON_ETAT, ConsoleType.NINTENDO_64, 12, jeux, "https://upload.wikimedia.org/wikipedia/en/9/9a/Mario_Kart_64_box.png"),
                createProduct("Pokemon Rouge (Game Boy)", "pokemon-rouge-game-boy", "La version rouge du RPG culte", new BigDecimal("39.99"), ProductCondition.BON_ETAT, ConsoleType.GAMEBOY, 8, jeux, "https://upload.wikimedia.org/wikipedia/en/4/46/Pok%C3%A9mon_Red_Version_box_art.jpg"),
                createProduct("Super Smash Bros Melee (GC)", "super-smash-bros-melee", "Le jeu de combat ultimate sur GameCube", new BigDecimal("44.99"), ProductCondition.NEUF, ConsoleType.GAMECUBE, 10, jeux, "https://upload.wikimedia.org/wikipedia/en/0/0b/Super_Smash_Bros_Melee_box_art.png"),
                createProduct("Wii Sports", "wii-sports", "Le jeu inclus avec la Wii", new BigDecimal("14.99"), ProductCondition.RECONDITIONNE, ConsoleType.WII, 50, jeux, "https://upload.wikimedia.org/wikipedia/en/1/1a/Wii_Sports_European_box_art.jpg"),
                createProduct("Gran Turismo 4 (PS2)", "gran-turismo-4-ps2", "Simulation de course realiste", new BigDecimal("12.99"), ProductCondition.BON_ETAT, ConsoleType.PS2, 25, jeux, "https://upload.wikimedia.org/wikipedia/en/7/71/Gran_Turismo_4_box_art.jpg"),
                createProduct("The Last of Us (PS3)", "the-last-of-us-ps3", "Aventure post-apocalyptique acclaimee", new BigDecimal("9.99"), ProductCondition.RECONDITIONNE, ConsoleType.PS3, 20, jeux, "https://upload.wikimedia.org/wikipedia/en/4/46/Video_Game_Cover_-_The_Last_of_Us.jpg"),
                createProduct("Sonic the Hedgehog 2 (MD)", "sonic-2-mega-drive", "Le classique de Sega sur Mega Drive", new BigDecimal("14.99"), ProductCondition.BON_ETAT, ConsoleType.SEGA_MEGA_DRIVE, 18, jeux, "https://upload.wikimedia.org/wikipedia/en/8/8d/Sonic_the_Hedgehog_2_Box_Art.jpg"),

                createProduct("Manette NES Officielle", "manette-nes-officielle", "Manette NES originale grise", new BigDecimal("19.99"), ProductCondition.RECONDITIONNE, ConsoleType.NES, 25, manettes, "https://upload.wikimedia.org/wikipedia/commons/4/4a/NES_controller.JPG"),
                createProduct("Manette SNES Officielle", "manette-snes-officielle", "Manette SNES originale", new BigDecimal("24.99"), ProductCondition.BON_ETAT, ConsoleType.SNES, 20, manettes, "https://upload.wikimedia.org/wikipedia/commons/3/31/SNES-Controller-in-Hand.jpg"),
                createProduct("Manette N64 Officielle", "manette-n64-officielle", "Manette Nintendo 64 grise", new BigDecimal("29.99"), ProductCondition.BON_ETAT, ConsoleType.NINTENDO_64, 10, manettes, "https://upload.wikimedia.org/wikipedia/commons/5/53/Nintendo-64-Controller-Gray.jpg"),
                createProduct("Manette PS2 DualShock 2", "manette-ps2-dualshock-2", "Manette filaire PS2 noire", new BigDecimal("14.99"), ProductCondition.RECONDITIONNE, ConsoleType.PS2, 30, manettes, "https://upload.wikimedia.org/wikipedia/commons/8/8e/PS2-DualShock2-Controller.jpg"),
                createProduct("Manette GameCube Officielle", "manette-gamecube-officielle", "Manette GameCube indigo", new BigDecimal("34.99"), ProductCondition.BON_ETAT, ConsoleType.GAMECUBE, 8, manettes, "https://upload.wikimedia.org/wikipedia/commons/a/a5/GameCube_controller.png"),

                createProduct("Carte Memoire PS2 8 Mo", "carte-memoire-ps2-8mo", "Carte memoire officielle PS2", new BigDecimal("9.99"), ProductCondition.NEUF, ConsoleType.PS2, 40, accessoires, "https://upload.wikimedia.org/wikipedia/commons/f/f9/Sony-PS2-Memory-Card.jpg"),
                createProduct("Adaptateur HDMI Retro", "adaptateur-hdmi-retro", "Convertisseur HDMI pour consoles retro (RCA)", new BigDecimal("19.99"), ProductCondition.NEUF, ConsoleType.AUTRE, 35, accessoires, null),
                createProduct("Chargeur Console Portable", "chargeur-console-portable", "Chargeur universel USB pour consoles portables", new BigDecimal("12.99"), ProductCondition.NEUF, ConsoleType.AUTRE, 45, accessoires, null),

                createProduct("T-Shirt Retro Gamer", "t-shirt-retro-gamer", "T-shirt 100% coton motif pixel art", new BigDecimal("24.99"), ProductCondition.NEUF, ConsoleType.AUTRE, 50, goodies, null),
                createProduct("Poster Carte du Monde Retro Gaming", "poster-carte-monde-retro", "Poster A2 des consoles cultes par pays", new BigDecimal("14.99"), ProductCondition.NEUF, ConsoleType.AUTRE, 30, goodies, null),
                createProduct("Mug Pixel Heart", "mug-pixel-heart", "Mug ceramique avec coeur pixel", new BigDecimal("9.99"), ProductCondition.NEUF, ConsoleType.AUTRE, 60, goodies, null)
        );

        productRepository.saveAll(products);

        log.info("Seeding complete: {} users, {} categories, {} products",
                userRepository.count(), categoryRepository.count(), productRepository.count());
    }

    private Category createCategory(String name, String slug, String description) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        c.setDescription(description);
        return c;
    }

    private Product createProduct(String name, String slug, String description, BigDecimal price,
                                   ProductCondition condition, ConsoleType consoleType, int stock,
                                   Category category, String imageUrl) {
        Product p = new Product();
        p.setName(name);
        p.setSlug(slug);
        p.setDescription(description);
        p.setPrice(price);
        p.setCondition(condition);
        p.setConsoleType(consoleType);
        p.setStockQuantity(stock);
        p.setCategory(category);
        p.setImageUrl(imageUrl);
        return p;
    }
}
