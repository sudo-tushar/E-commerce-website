package com.ecommerce.controller;

import com.ecommerce.dto.request.AddToCartRequest;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000"})
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestHeader("Firebase-UID") String firebaseUid) {
        return cartService.getCart(firebaseUid)
                .map(cart -> ResponseEntity.ok(cart))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @RequestHeader("Firebase-UID") String firebaseUid,
            @Valid @RequestBody AddToCartRequest request) {
        
        CartResponse response = cartService.addToCart(firebaseUid, request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @RequestHeader("Firebase-UID") String firebaseUid,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        
        CartResponse response = cartService.updateCartItem(firebaseUid, cartItemId, quantity);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @RequestHeader("Firebase-UID") String firebaseUid,
            @PathVariable Long cartItemId) {
        
        CartResponse response = cartService.removeFromCart(firebaseUid, cartItemId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader("Firebase-UID") String firebaseUid) {
        cartService.clearCart(firebaseUid);
        return ResponseEntity.ok().build();
    }
}
