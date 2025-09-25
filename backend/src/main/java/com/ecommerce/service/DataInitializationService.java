package com.ecommerce.service;

import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {
    
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    
    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            log.info("Initializing sample data...");
            initializeCategories();
            initializeProducts();
            initializeAdminUser();
            log.info("Sample data initialization completed.");
        }
    }
    
    private void initializeCategories() {
        // Create main categories
        Category electronics = createCategory("Electronics", "Latest electronic gadgets and devices", "electronics");
        Category clothing = createCategory("Clothing", "Fashionable apparel for all occasions", "clothing");
        Category home = createCategory("Home & Garden", "Everything for your home and garden", "home-garden");
        Category sports = createCategory("Sports & Outdoors", "Sports equipment and outdoor gear", "sports-outdoors");
        Category books = createCategory("Books", "Wide selection of books across all genres", "books");
        
        // Create subcategories
        createCategory("Smartphones", "Latest smartphones and accessories", "smartphones", electronics);
        createCategory("Laptops", "Computers and laptops", "laptops", electronics);
        createCategory("Headphones", "Audio equipment and headphones", "headphones", electronics);
        
        createCategory("Men's Clothing", "Clothing for men", "mens-clothing", clothing);
        createCategory("Women's Clothing", "Clothing for women", "womens-clothing", clothing);
        createCategory("Shoes", "Footwear for all", "shoes", clothing);
        
        log.info("Categories initialized successfully");
    }
    
    private Category createCategory(String name, String description, String slug) {
        return createCategory(name, description, slug, null);
    }
    
    private Category createCategory(String name, String description, String slug, Category parent) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setSlug(slug);
        category.setParent(parent);
        category.setActive(true);
        category.setSortOrder(0);
        return categoryRepository.save(category);
    }
    
    private void initializeProducts() {
        // Get categories
        Category electronics = categoryRepository.findBySlug("electronics").orElse(null);
        Category smartphones = categoryRepository.findBySlug("smartphones").orElse(null);
        Category laptops = categoryRepository.findBySlug("laptops").orElse(null);
        Category clothing = categoryRepository.findBySlug("clothing").orElse(null);
        Category mensClothing = categoryRepository.findBySlug("mens-clothing").orElse(null);
        
        // Create sample products
        createProduct(
            "iPhone 15 Pro",
            "Latest iPhone with advanced camera system",
            "The iPhone 15 Pro features the powerful A17 Pro chip, titanium design, and revolutionary camera system.",
            "iphone-15-pro",
            new BigDecimal("999.00"),
            null,
            50,
            "APPLE001",
            "Apple",
            smartphones,
            Arrays.asList(
                "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=500",
                "https://images.unsplash.com/photo-1591337676887-a217a6970a8a?w=500"
            ),
            Arrays.asList("smartphone", "apple", "iphone", "mobile"),
            true
        );
        
        createProduct(
            "MacBook Pro 16\"",
            "Powerful laptop for professionals",
            "MacBook Pro with M3 Pro chip delivers exceptional performance for demanding workflows.",
            "macbook-pro-16",
            new BigDecimal("2399.00"),
            new BigDecimal("2199.00"),
            25,
            "APPLE002",
            "Apple",
            laptops,
            Arrays.asList(
                "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=500",
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500"
            ),
            Arrays.asList("laptop", "apple", "macbook", "computer"),
            true
        );
        
        createProduct(
            "Samsung Galaxy S24 Ultra",
            "Flagship Android smartphone",
            "Samsung Galaxy S24 Ultra with S Pen, advanced camera system, and powerful performance.",
            "samsung-galaxy-s24-ultra",
            new BigDecimal("1199.00"),
            new BigDecimal("1099.00"),
            30,
            "SAMSUNG001",
            "Samsung",
            smartphones,
            Arrays.asList(
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500"
            ),
            Arrays.asList("smartphone", "samsung", "galaxy", "android"),
            false
        );
        
        createProduct(
            "Dell XPS 13",
            "Ultra-portable laptop",
            "Dell XPS 13 with Intel Core processor, stunning display, and premium build quality.",
            "dell-xps-13",
            new BigDecimal("1299.00"),
            null,
            15,
            "DELL001",
            "Dell",
            laptops,
            Arrays.asList(
                "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=500"
            ),
            Arrays.asList("laptop", "dell", "xps", "ultrabook"),
            false
        );
        
        createProduct(
            "Classic Cotton T-Shirt",
            "Comfortable everyday t-shirt",
            "Premium cotton t-shirt with perfect fit and long-lasting comfort.",
            "classic-cotton-tshirt",
            new BigDecimal("24.99"),
            new BigDecimal("19.99"),
            100,
            "SHIRT001",
            "ClassicWear",
            mensClothing,
            Arrays.asList(
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500"
            ),
            Arrays.asList("t-shirt", "cotton", "casual", "clothing"),
            false
        );
        
        createProduct(
            "Wireless Bluetooth Headphones",
            "Premium wireless headphones",
            "High-quality wireless headphones with noise cancellation and long battery life.",
            "wireless-bluetooth-headphones",
            new BigDecimal("199.99"),
            new BigDecimal("149.99"),
            75,
            "AUDIO001",
            "SoundTech",
            electronics,
            Arrays.asList(
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500"
            ),
            Arrays.asList("headphones", "wireless", "bluetooth", "audio"),
            true
        );
        
        log.info("Products initialized successfully");
    }
    
    private void createProduct(String name, String description, String detailedDescription,
                             String slug, BigDecimal price, BigDecimal salePrice, int stockQuantity,
                             String sku, String brand, Category category, List<String> imageUrls,
                             List<String> tags, boolean isFeatured) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setDetailedDescription(detailedDescription);
        product.setSlug(slug);
        product.setPrice(price);
        product.setSalePrice(salePrice);
        product.setStockQuantity(stockQuantity);
        product.setSku(sku);
        product.setBrand(brand);
        product.setCategory(category);
        product.setImageUrls(imageUrls);
        product.setTags(tags);
        product.setStatus(Product.ProductStatus.ACTIVE);
        product.setFeatured(isFeatured);
        product.setAverageRating(4.5);
        product.setReviewCount(25);
        
        productRepository.save(product);
    }
    
    private void initializeAdminUser() {
        // Create admin user (Note: In real implementation, this would be created via registration)
        if (!userRepository.existsByEmail("admin@ecommerce.com")) {
            User admin = new User();
            admin.setFirebaseUid("admin-uid-demo");
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@ecommerce.com");
            admin.setRole(User.UserRole.ADMIN);
            admin.setActive(true);
            
            userRepository.save(admin);
            
            // Create cart for admin
            Cart cart = new Cart();
            cart.setUser(admin);
            cartRepository.save(cart);
            
            log.info("Admin user created: admin@ecommerce.com");
        }
    }
}
