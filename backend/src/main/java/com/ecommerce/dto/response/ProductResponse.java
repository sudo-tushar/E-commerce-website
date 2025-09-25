package com.ecommerce.dto.response;

import com.ecommerce.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String detailedDescription;
    private String slug;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private String sku;
    private String brand;
    private Product.ProductStatus status;
    private boolean isFeatured;
    private Double weight;
    private String dimensions;
    private List<String> imageUrls;
    private List<String> tags;
    private CategoryResponse category;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private String imageUrl;
        
        public static CategoryResponse fromEntity(com.ecommerce.entity.Category category) {
            if (category == null) return null;
            
            CategoryResponse response = new CategoryResponse();
            response.setId(category.getId());
            response.setName(category.getName());
            response.setSlug(category.getSlug());
            response.setDescription(category.getDescription());
            response.setImageUrl(category.getImageUrl());
            return response;
        }
    }
    
    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setDetailedDescription(product.getDetailedDescription());
        response.setSlug(product.getSlug());
        response.setPrice(product.getPrice());
        response.setSalePrice(product.getSalePrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setSku(product.getSku());
        response.setBrand(product.getBrand());
        response.setStatus(product.getStatus());
        response.setFeatured(product.isFeatured());
        response.setWeight(product.getWeight());
        response.setDimensions(product.getDimensions());
        response.setImageUrls(product.getImageUrls());
        response.setTags(product.getTags());
        response.setCategory(CategoryResponse.fromEntity(product.getCategory()));
        response.setAverageRating(product.getAverageRating());
        response.setReviewCount(product.getReviewCount());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
