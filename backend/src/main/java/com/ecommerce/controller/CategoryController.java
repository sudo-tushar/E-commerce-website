package com.ecommerce.controller;

import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000"})
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/top-level")
    public ResponseEntity<List<CategoryResponse>> getTopLevelCategories() {
        List<CategoryResponse> categories = categoryService.getTopLevelCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(category -> ResponseEntity.ok(category))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return categoryService.getCategoryBySlug(slug)
                .map(category -> ResponseEntity.ok(category))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubCategories(@PathVariable Long id) {
        List<CategoryResponse> subCategories = categoryService.getSubCategories(id);
        return ResponseEntity.ok(subCategories);
    }
    
    @GetMapping("/{id}/product-count")
    public ResponseEntity<Long> getProductCount(@PathVariable Long id) {
        long count = categoryService.getProductCount(id);
        return ResponseEntity.ok(count);
    }
}
