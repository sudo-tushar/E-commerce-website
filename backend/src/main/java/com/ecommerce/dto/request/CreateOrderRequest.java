package com.ecommerce.dto.request;

import com.ecommerce.entity.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    
    @NotNull(message = "Payment method is required")
    private Order.PaymentMethod paymentMethod;
    
    // Shipping Address
    @NotBlank(message = "Shipping street is required")
    private String shippingStreet;
    
    @NotBlank(message = "Shipping city is required")
    private String shippingCity;
    
    @NotBlank(message = "Shipping state is required")
    private String shippingState;
    
    @NotBlank(message = "Shipping country is required")
    private String shippingCountry;
    
    @NotBlank(message = "Shipping postal code is required")
    private String shippingPostalCode;
    
    // Billing Address
    @NotBlank(message = "Billing street is required")
    private String billingStreet;
    
    @NotBlank(message = "Billing city is required")
    private String billingCity;
    
    @NotBlank(message = "Billing state is required")
    private String billingState;
    
    @NotBlank(message = "Billing country is required")
    private String billingCountry;
    
    @NotBlank(message = "Billing postal code is required")
    private String billingPostalCode;
    
    private String notes;
    
    // Payment details
    private String paymentIntentId; // For Stripe integration
}
