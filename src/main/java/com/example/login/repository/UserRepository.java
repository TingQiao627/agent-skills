package com.example.login.repository;

import com.example.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByPhone(String phone);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    default Optional<User> findByLoginField(String loginField) {
        if (loginField.contains("@")) {
            return findByEmail(loginField);
        } else if (loginField.matches("\\d{11}")) {
            return findByPhone(loginField);
        } else {
            return findByUsername(loginField);
        }
    }
}