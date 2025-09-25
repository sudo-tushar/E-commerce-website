package com.ecommerce.dto.response;

import com.ecommerce.entity.Category;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private String imageUrl;
    private boolean isActive;
    private Integer sortOrder;
    private Long parentId;
    private String parentName;
    private List<CategoryResponse> children;
    private long productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static CategoryResponse fromEntity(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setSlug(category.getSlug());
        response.setImageUrl(category.getImageUrl());
        response.setActive(category.isActive());
        response.setSortOrder(category.getSortOrder());
        
        if (category.getParent() != null) {
            response.setParentId(category.getParent().getId());
            response.setParentName(category.getParent().getName());
        }
        
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            response.setChildren(category.getChildren().stream()
                    .filter(Category::isActive)
                    .map(CategoryResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        return response;
    }
}
