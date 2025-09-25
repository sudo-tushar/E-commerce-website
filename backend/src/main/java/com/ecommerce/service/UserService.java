package com.ecommerce.service;

import com.ecommerce.dto.request.UserRegistrationRequest;
import com.ecommerce.dto.response.UserResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    
    public UserResponse registerUser(UserRegistrationRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail()) || 
            userRepository.existsByFirebaseUid(request.getFirebaseUid())) {
            throw new RuntimeException("User already exists");
        }
        
        // Create new user
        User user = new User();
        user.setFirebaseUid(request.getFirebaseUid());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(User.UserRole.CUSTOMER);
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        
        // Create cart for new user
        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);
        
        log.info("User registered: {}", savedUser.getEmail());
        return UserResponse.fromEntity(savedUser);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(UserResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponse::fromEntity);
    }
    
    public UserResponse updateUser(Long id, UserRegistrationRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        
        // Don't allow email or firebaseUid changes through this method
        User updatedUser = userRepository.save(user);
        
        log.info("User updated: {}", updatedUser.getEmail());
        return UserResponse.fromEntity(updatedUser);
    }
    
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setActive(false);
        userRepository.save(user);
        
        log.info("User deactivated: {}", user.getEmail());
    }
    
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setActive(true);
        userRepository.save(user);
        
        log.info("User activated: {}", user.getEmail());
    }
    
    @Transactional(readOnly = true)
    public boolean isUserActive(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(User::isActive)
                .orElse(false);
    }
    
    @Transactional(readOnly = true)
    public boolean isAdmin(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(user -> user.getRole() == User.UserRole.ADMIN)
                .orElse(false);
    }
    
    @Transactional(readOnly = true)
    public long getActiveCustomerCount() {
        return userRepository.countActiveCustomers();
    }
    
    @Transactional(readOnly = true)
    public long getActiveAdminCount() {
        return userRepository.countActiveAdmins();
    }
}
