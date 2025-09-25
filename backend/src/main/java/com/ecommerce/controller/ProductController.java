package com.ecommerce.controller;

import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000"})
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<ProductResponse> products = productService.getAllProducts(page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(product))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        return productService.getProductBySlug(slug)
                .map(product -> ResponseEntity.ok(product))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Page<ProductResponse> products = productService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        
        List<ProductResponse> products = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Page<ProductResponse> products = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/filter/price")
    public ResponseEntity<Page<ProductResponse>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Page<ProductResponse> products = productService.getProductsByPriceRange(minPrice, maxPrice, page, size);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/filter/brand")
    public ResponseEntity<Page<ProductResponse>> getProductsByBrand(
            @RequestParam String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Page<ProductResponse> products = productService.getProductsByBrand(brand, page, size);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        List<String> brands = productService.getAllBrands();
        return ResponseEntity.ok(brands);
    }
    
    @GetMapping("/latest")
    public ResponseEntity<List<ProductResponse>> getLatestProducts(
            @RequestParam(defaultValue = "8") int limit) {
        
        List<ProductResponse> products = productService.getLatestProducts(limit);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/top-rated")
    public ResponseEntity<List<ProductResponse>> getTopRatedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        
        List<ProductResponse> products = productService.getTopRatedProducts(limit);
        return ResponseEntity.ok(products);
    }
}
