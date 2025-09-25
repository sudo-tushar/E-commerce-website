package com.ecommerce.dto.response;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private List<OrderItemResponse> items;
    private Order.OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private Order.PaymentMethod paymentMethod;
    private Order.PaymentStatus paymentStatus;
    private String paymentIntentId;
    private String trackingNumber;
    private ShippingAddress shippingAddress;
    private BillingAddress billingAddress;
    private String notes;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private String productImageUrl;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
        
        public static OrderItemResponse fromEntity(OrderItem orderItem) {
            OrderItemResponse response = new OrderItemResponse();
            response.setId(orderItem.getId());
            response.setProductId(orderItem.getProduct().getId());
            response.setProductName(orderItem.getProductName());
            response.setProductSku(orderItem.getProductSku());
            response.setProductImageUrl(orderItem.getProductImageUrl());
            response.setUnitPrice(orderItem.getUnitPrice());
            response.setQuantity(orderItem.getQuantity());
            response.setTotalPrice(orderItem.getTotalPrice());
            return response;
        }
    }
    
    @Data
    public static class ShippingAddress {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;
    }
    
    @Data
    public static class BillingAddress {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;
    }
    
    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        
        if (order.getItems() != null) {
            response.setItems(order.getItems().stream()
                    .map(OrderItemResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        response.setStatus(order.getStatus());
        response.setSubtotal(order.getSubtotal());
        response.setTax(order.getTax());
        response.setShippingCost(order.getShippingCost());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setPaymentIntentId(order.getPaymentIntentId());
        response.setTrackingNumber(order.getTrackingNumber());
        
        // Set shipping address
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setStreet(order.getShippingStreet());
        shippingAddress.setCity(order.getShippingCity());
        shippingAddress.setState(order.getShippingState());
        shippingAddress.setCountry(order.getShippingCountry());
        shippingAddress.setPostalCode(order.getShippingPostalCode());
        response.setShippingAddress(shippingAddress);
        
        // Set billing address
        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setStreet(order.getBillingStreet());
        billingAddress.setCity(order.getBillingCity());
        billingAddress.setState(order.getBillingState());
        billingAddress.setCountry(order.getBillingCountry());
        billingAddress.setPostalCode(order.getBillingPostalCode());
        response.setBillingAddress(billingAddress);
        
        response.setNotes(order.getNotes());
        response.setShippedAt(order.getShippedAt());
        response.setDeliveredAt(order.getDeliveredAt());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        
        return response;
    }
}
