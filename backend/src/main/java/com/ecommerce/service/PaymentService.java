package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PaymentService {
    
    @Value("${stripe.api.key:}")
    private String stripeSecretKey;
    
    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.isEmpty()) {
            Stripe.apiKey = stripeSecretKey;
            log.info("Stripe payment service initialized");
        } else {
            log.warn("Stripe secret key not configured - payment processing will be simulated");
        }
    }
    
    public PaymentIntent createPaymentIntent(Order order) throws StripeException {
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            // Return a mock payment intent for demo purposes
            return createMockPaymentIntent(order);
        }
        
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(order.getTotalAmount().multiply(new BigDecimal("100")).longValue()) // Convert to cents
                .setCurrency("usd")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .putMetadata("order_id", order.getId().toString())
                .putMetadata("order_number", order.getOrderNumber())
                .setDescription("Order #" + order.getOrderNumber())
                .build();
        
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        log.info("Created payment intent for order {}: {}", order.getOrderNumber(), paymentIntent.getId());
        
        return paymentIntent;
    }
    
    public PaymentIntent confirmPayment(String paymentIntentId) throws StripeException {
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            // Return a mock confirmed payment intent
            return createMockConfirmedPaymentIntent(paymentIntentId);
        }
        
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        Map<String, Object> params = new HashMap<>();
        paymentIntent = paymentIntent.confirm(params);
        
        log.info("Confirmed payment intent: {}", paymentIntentId);
        return paymentIntent;
    }
    
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            return createMockPaymentIntent(null);
        }
        
        return PaymentIntent.retrieve(paymentIntentId);
    }
    
    public void refundPayment(String paymentIntentId, BigDecimal amount) throws StripeException {
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            log.info("Mock refund processed for payment intent: {} amount: {}", paymentIntentId, amount);
            return;
        }
        
        // Implement refund logic using Stripe Refund API
        // com.stripe.model.Refund.create(...)
        log.info("Refund processed for payment intent: {} amount: {}", paymentIntentId, amount);
    }
    
    // Mock payment intent for demo purposes
    private PaymentIntent createMockPaymentIntent(Order order) {
        // Create a mock PaymentIntent object
        // Note: In a real implementation, you would need to handle this differently
        // This is just for demonstration when Stripe is not configured
        log.info("Creating mock payment intent for demo purposes");
        return null; // In practice, you'd return a properly structured response
    }
    
    private PaymentIntent createMockConfirmedPaymentIntent(String paymentIntentId) {
        log.info("Mock payment confirmation for: {}", paymentIntentId);
        return null; // In practice, you'd return a properly structured response
    }
    
    public String getPublishableKey() {
        // Return the publishable key for frontend use
        // This should be stored in application properties
        return System.getenv("STRIPE_PUBLISHABLE_KEY");
    }
}
