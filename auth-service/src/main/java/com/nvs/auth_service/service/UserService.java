package com.nvs.auth_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nvs.auth_service.dto.request.RegisterRequest;
import com.nvs.auth_service.dto.request.UserRequest;
import com.nvs.auth_service.dto.response.UserResponse;
import com.nvs.auth_service.entity.User;
import com.nvs.auth_service.repository.UserRepository;

// import jakarta.validation.Valid;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User save(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole("USER"); // Default role
        return repo.save(user);
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    public User login(String email, String password) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return user;
    }

    public UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.id = user.getId();
        res.username = user.getUsername();
        res.email = user.getEmail();
        return res;
    }
}