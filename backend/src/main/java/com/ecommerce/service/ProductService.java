package com.ecommerce.service;

import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable)
                .map(ProductResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductById(Long id) {
        return productRepository.findById(id)
                .filter(product -> product.getStatus() == Product.ProductStatus.ACTIVE)
                .map(ProductResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .filter(product -> product.getStatus() == Product.ProductStatus.ACTIVE)
                .map(ProductResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByCategoryIdAndStatus(categoryId, Product.ProductStatus.ACTIVE, pageable)
                .map(ProductResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findFeaturedProducts(pageable)
                .stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.searchProducts(keyword, pageable)
                .map(ProductResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable)
                .map(ProductResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByBrand(String brand, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByBrand(brand, pageable)
                .map(ProductResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public List<String> getAllBrands() {
        return productRepository.findAllBrands();
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getLatestProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findLatestProducts(pageable)
                .stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getTopRatedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findTopRatedProducts(pageable)
                .stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public boolean isProductAvailable(Long productId, int quantity) {
        return productRepository.findById(productId)
                .map(product -> product.getStatus() == Product.ProductStatus.ACTIVE && 
                               product.getStockQuantity() >= quantity)
                .orElse(false);
    }
    
    public void updateStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }
        
        product.setStockQuantity(product.getStockQuantity() - quantity);
        
        // Update status if out of stock
        if (product.getStockQuantity() == 0) {
            product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
        }
        
        productRepository.save(product);
        log.info("Updated stock for product {}: -{} units", productId, quantity);
    }
    
    public void restoreStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setStockQuantity(product.getStockQuantity() + quantity);
        
        // Restore status if previously out of stock
        if (product.getStatus() == Product.ProductStatus.OUT_OF_STOCK && product.getStockQuantity() > 0) {
            product.setStatus(Product.ProductStatus.ACTIVE);
        }
        
        productRepository.save(product);
        log.info("Restored stock for product {}: +{} units", productId, quantity);
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public long getActiveProductCount() {
        return productRepository.countActiveProducts();
    }
    
    // Admin methods for product management
    public ProductResponse createProduct(Product product) {
        // Generate slug from name
        product.setSlug(generateSlug(product.getName()));
        
        Product savedProduct = productRepository.save(product);
        log.info("Created product: {}", savedProduct.getName());
        return ProductResponse.fromEntity(savedProduct);
    }
    
    public ProductResponse updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setDetailedDescription(updatedProduct.getDetailedDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setSalePrice(updatedProduct.getSalePrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
        existingProduct.setBrand(updatedProduct.getBrand());
        existingProduct.setStatus(updatedProduct.getStatus());
        existingProduct.setFeatured(updatedProduct.isFeatured());
        existingProduct.setImageUrls(updatedProduct.getImageUrls());
        existingProduct.setTags(updatedProduct.getTags());
        existingProduct.setCategory(updatedProduct.getCategory());
        
        // Update slug if name changed
        if (!existingProduct.getName().equals(updatedProduct.getName())) {
            existingProduct.setSlug(generateSlug(updatedProduct.getName()));
        }
        
        Product savedProduct = productRepository.save(existingProduct);
        log.info("Updated product: {}", savedProduct.getName());
        return ProductResponse.fromEntity(savedProduct);
    }
    
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setStatus(Product.ProductStatus.INACTIVE);
        productRepository.save(product);
        log.info("Deleted product: {}", product.getName());
    }
    
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
