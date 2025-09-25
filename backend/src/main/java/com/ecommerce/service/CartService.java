package com.ecommerce.service;

import com.ecommerce.dto.request.AddToCartRequest;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    
    public CartResponse addToCart(String firebaseUid, AddToCartRequest request) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if product is available
        if (!productService.isProductAvailable(product.getId(), request.getQuantity())) {
            throw new RuntimeException("Product is not available or insufficient stock");
        }
        
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
        
        // Check if product already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(
                cart.getId(), product.getId());
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            // Check if new quantity is available
            if (!productService.isProductAvailable(product.getId(), newQuantity)) {
                throw new RuntimeException("Insufficient stock for requested quantity");
            }
            
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(product.getSalePrice() != null ? product.getSalePrice() : product.getPrice());
        }
        
        cartItemRepository.save(cartItem);
        updateCartTotals(cart);
        
        log.info("Added product {} to cart for user {}", product.getId(), firebaseUid);
        return CartResponse.fromEntity(cartRepository.findByUserIdWithItems(user.getId()).orElse(cart));
    }
    
    public CartResponse updateCartItem(String firebaseUid, Long cartItemId, Integer quantity) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        // Verify the cart item belongs to the user
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Cart item does not belong to user");
        }
        
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            log.info("Removed cart item {} for user {}", cartItemId, firebaseUid);
        } else {
            // Check if quantity is available
            if (!productService.isProductAvailable(cartItem.getProduct().getId(), quantity)) {
                throw new RuntimeException("Insufficient stock for requested quantity");
            }
            
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            log.info("Updated cart item {} quantity to {} for user {}", cartItemId, quantity, firebaseUid);
        }
        
        Cart cart = cartItem.getCart();
        updateCartTotals(cart);
        
        return CartResponse.fromEntity(cartRepository.findByUserIdWithItems(user.getId()).orElse(cart));
    }
    
    public CartResponse removeFromCart(String firebaseUid, Long cartItemId) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        // Verify the cart item belongs to the user
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Cart item does not belong to user");
        }
        
        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);
        updateCartTotals(cart);
        
        log.info("Removed cart item {} for user {}", cartItemId, firebaseUid);
        return CartResponse.fromEntity(cartRepository.findByUserIdWithItems(user.getId()).orElse(cart));
    }
    
    @Transactional(readOnly = true)
    public Optional<CartResponse> getCart(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return cartRepository.findByUserIdWithItems(user.getId())
                .map(CartResponse::fromEntity);
    }
    
    public void clearCart(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        cartItemRepository.deleteByCartId(cart.getId());
        updateCartTotals(cart);
        
        log.info("Cleared cart for user {}", firebaseUid);
    }
    
    private void updateCartTotals(Cart cart) {
        cart = cartRepository.findByUserIdWithItems(cart.getUser().getId()).orElse(cart);
        
        BigDecimal totalAmount = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Integer totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        
        cart.setTotalAmount(totalAmount);
        cart.setTotalItems(totalItems);
        cartRepository.save(cart);
    }
}
