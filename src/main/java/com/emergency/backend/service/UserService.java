package com.emergency.backend.service;

import org.springframework.stereotype.Service;

import com.emergency.backend.entity.User;
import com.emergency.backend.repository.UserRepository;
import com.emergency.backend.config.JwtUtil;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // 🔥 ดึง user จาก token
    public User getUserFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);

        return findById(Long.parseLong(userId));
    }

    // 🔥 ใช้แบบ safe + ใช้จริงในระบบ
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}