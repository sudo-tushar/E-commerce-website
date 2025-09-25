package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column
    private String paymentIntentId;

    @Column
    private String trackingNumber;

    // Shipping Address
    @Column(nullable = false)
    @NotBlank(message = "Shipping street is required")
    private String shippingStreet;

    @Column(nullable = false)
    @NotBlank(message = "Shipping city is required")
    private String shippingCity;

    @Column(nullable = false)
    @NotBlank(message = "Shipping state is required")
    private String shippingState;

    @Column(nullable = false)
    @NotBlank(message = "Shipping country is required")
    private String shippingCountry;

    @Column(nullable = false)
    @NotBlank(message = "Shipping postal code is required")
    private String shippingPostalCode;

    // Billing Address
    @Column(nullable = false)
    @NotBlank(message = "Billing street is required")
    private String billingStreet;

    @Column(nullable = false)
    @NotBlank(message = "Billing city is required")
    private String billingCity;

    @Column(nullable = false)
    @NotBlank(message = "Billing state is required")
    private String billingState;

    @Column(nullable = false)
    @NotBlank(message = "Billing country is required")
    private String billingCountry;

    @Column(nullable = false)
    @NotBlank(message = "Billing postal code is required")
    private String billingPostalCode;

    @Column
    private String notes;

    @Column
    private LocalDateTime shippedAt;

    @Column
    private LocalDateTime deliveredAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }

    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, PAYPAL
    }

    public enum PaymentStatus {
        PENDING, PAID, FAILED, REFUNDED, PARTIALLY_REFUNDED
    }
}
