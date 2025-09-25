package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByFirebaseUid(String firebaseUid);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByFirebaseUid(String firebaseUid);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    Iterable<User> findActiveUsersByRole(@Param("role") User.UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER' AND u.isActive = true")
    long countActiveCustomers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN' AND u.isActive = true")
    long countActiveAdmins();
}
