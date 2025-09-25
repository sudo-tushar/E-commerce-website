package com.ecommerce.service;

import com.ecommerce.dto.request.CreateOrderRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final CartService cartService;
    
    public OrderResponse createOrder(String firebaseUid, CreateOrderRequest request) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Cart cart = cartRepository.findByUserIdWithItems(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found or is empty"));
        
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Validate all items are still available
        for (CartItem cartItem : cart.getItems()) {
            if (!productService.isProductAvailable(cartItem.getProduct().getId(), cartItem.getQuantity())) {
                throw new RuntimeException("Product " + cartItem.getProduct().getName() + " is no longer available in requested quantity");
            }
        }
        
        // Create order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        
        // Set addresses
        order.setShippingStreet(request.getShippingStreet());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingCountry(request.getShippingCountry());
        order.setShippingPostalCode(request.getShippingPostalCode());
        
        order.setBillingStreet(request.getBillingStreet());
        order.setBillingCity(request.getBillingCity());
        order.setBillingState(request.getBillingState());
        order.setBillingCountry(request.getBillingCountry());
        order.setBillingPostalCode(request.getBillingPostalCode());
        
        order.setNotes(request.getNotes());
        
        // Calculate totals
        BigDecimal subtotal = cart.getTotalAmount();
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.08)); // 8% tax
        BigDecimal shippingCost = BigDecimal.valueOf(10.00); // Flat shipping
        BigDecimal totalAmount = subtotal.add(tax).add(shippingCost);
        
        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setShippingCost(shippingCost);
        order.setTotalAmount(totalAmount);
        
        Order savedOrder = orderRepository.save(order);
        
        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setTotalPrice(cartItem.getTotalPrice());
            
            orderItems.add(orderItem);
        }
        
        orderItemRepository.saveAll(orderItems);
        savedOrder.setItems(orderItems);
        
        // Update product stock
        for (CartItem cartItem : cart.getItems()) {
            productService.updateStock(cartItem.getProduct().getId(), cartItem.getQuantity());
        }
        
        // Clear cart
        cartService.clearCart(firebaseUid);
        
        log.info("Created order {} for user {}", savedOrder.getOrderNumber(), firebaseUid);
        return OrderResponse.fromEntity(savedOrder);
    }
    
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(OrderResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(OrderResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(String firebaseUid, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByUserFirebaseUidOrderByCreatedAtDesc(firebaseUid, pageable)
                .map(OrderResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(Order.OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(OrderResponse::fromEntity);
    }
    
    public OrderResponse updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        
        // Set timestamps based on status
        switch (status) {
            case SHIPPED:
                order.setShippedAt(LocalDateTime.now());
                order.setTrackingNumber(generateTrackingNumber());
                break;
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case CANCELLED:
                // Restore stock for cancelled orders
                if (oldStatus != Order.OrderStatus.CANCELLED) {
                    restoreStockForOrder(order);
                }
                break;
        }
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Updated order {} status from {} to {}", orderId, oldStatus, status);
        
        return OrderResponse.fromEntity(updatedOrder);
    }
    
    public OrderResponse updatePaymentStatus(Long orderId, Order.PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setPaymentStatus(paymentStatus);
        
        // If payment is successful, confirm the order
        if (paymentStatus == Order.PaymentStatus.PAID && order.getStatus() == Order.OrderStatus.PENDING) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
        }
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Updated order {} payment status to {}", orderId, paymentStatus);
        
        return OrderResponse.fromEntity(updatedOrder);
    }
    
    public void cancelOrder(String firebaseUid, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Verify order belongs to user
        if (!order.getUser().getFirebaseUid().equals(firebaseUid)) {
            throw new RuntimeException("Order does not belong to user");
        }
        
        // Only allow cancellation of pending or confirmed orders
        if (order.getStatus() != Order.OrderStatus.PENDING && 
            order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order cannot be cancelled");
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        // Restore stock
        restoreStockForOrder(order);
        
        log.info("Cancelled order {} for user {}", orderId, firebaseUid);
    }
    
    private void restoreStockForOrder(Order order) {
        for (OrderItem orderItem : order.getItems()) {
            productService.restoreStock(orderItem.getProduct().getId(), orderItem.getQuantity());
        }
    }
    
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateTrackingNumber() {
        return "TRK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
