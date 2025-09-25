package com.ecommerce.dto.response;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    public static class CartItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productSlug;
        private String productImageUrl;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
        private boolean isAvailable;
        
        public static CartItemResponse fromEntity(CartItem cartItem) {
            CartItemResponse response = new CartItemResponse();
            response.setId(cartItem.getId());
            response.setProductId(cartItem.getProduct().getId());
            response.setProductName(cartItem.getProduct().getName());
            response.setProductSlug(cartItem.getProduct().getSlug());
            if (cartItem.getProduct().getImageUrls() != null && !cartItem.getProduct().getImageUrls().isEmpty()) {
                response.setProductImageUrl(cartItem.getProduct().getImageUrls().get(0));
            }
            response.setUnitPrice(cartItem.getUnitPrice());
            response.setQuantity(cartItem.getQuantity());
            response.setTotalPrice(cartItem.getTotalPrice());
            response.setAvailable(cartItem.getProduct().getStockQuantity() >= cartItem.getQuantity());
            return response;
        }
    }
    
    public static CartResponse fromEntity(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        if (cart.getItems() != null) {
            response.setItems(cart.getItems().stream()
                    .map(CartItemResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        response.setTotalAmount(cart.getTotalAmount());
        response.setTotalItems(cart.getTotalItems());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        return response;
    }
}
