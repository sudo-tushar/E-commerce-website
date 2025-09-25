package com.ecommerce.service;

import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrder()
                .stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CategoryResponse> getTopLevelCategories() {
        return categoryRepository.findTopLevelCategories()
                .stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<CategoryResponse> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .filter(Category::isActive)
                .map(CategoryResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public Optional<CategoryResponse> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .filter(Category::isActive)
                .map(CategoryResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubCategories(Long parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrue(parentId)
                .stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public long getProductCount(Long categoryId) {
        return categoryRepository.countProductsInCategory(categoryId);
    }
    
    // Admin methods
    public CategoryResponse createCategory(Category category) {
        // Generate slug from name
        category.setSlug(generateSlug(category.getName()));
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Created category: {}", savedCategory.getName());
        return CategoryResponse.fromEntity(savedCategory);
    }
    
    public CategoryResponse updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());
        existingCategory.setImageUrl(updatedCategory.getImageUrl());
        existingCategory.setSortOrder(updatedCategory.getSortOrder());
        existingCategory.setActive(updatedCategory.isActive());
        existingCategory.setParent(updatedCategory.getParent());
        
        // Update slug if name changed
        if (!existingCategory.getName().equals(updatedCategory.getName())) {
            existingCategory.setSlug(generateSlug(updatedCategory.getName()));
        }
        
        Category savedCategory = categoryRepository.save(existingCategory);
        log.info("Updated category: {}", savedCategory.getName());
        return CategoryResponse.fromEntity(savedCategory);
    }
    
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.setActive(false);
        categoryRepository.save(category);
        log.info("Deleted category: {}", category.getName());
    }
    
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
