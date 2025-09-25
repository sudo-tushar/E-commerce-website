package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findBySlug(String slug);
    
    List<Category> findByParentIsNullAndIsActiveTrue();
    
    List<Category> findByParentIdAndIsActiveTrue(Long parentId);
    
    List<Category> findByIsActiveTrueOrderBySortOrder();
    
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.sortOrder")
    List<Category> findTopLevelCategories();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.status = 'ACTIVE'")
    long countProductsInCategory(Long categoryId);
    
    boolean existsByName(String name);
    
    boolean existsBySlug(String slug);
}
