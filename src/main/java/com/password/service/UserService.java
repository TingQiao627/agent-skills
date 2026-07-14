package com.password.service;

import com.password.entity.User;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    public Optional<User> findByEmail(String email) { return Optional.empty(); }
    public Optional<User> findByPhone(String phone) { return Optional.empty(); }
    public Optional<User> findById(Long id) { return Optional.empty(); }
    public User save(User user) { return user; }
}