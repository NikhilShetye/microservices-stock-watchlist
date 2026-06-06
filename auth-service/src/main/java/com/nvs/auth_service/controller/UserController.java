package com.nvs.auth_service.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nvs.auth_service.dto.request.LoginRequest;
import com.nvs.auth_service.dto.request.RegisterRequest;
import com.nvs.auth_service.dto.response.LoginResponse;
import com.nvs.auth_service.entity.User;
import com.nvs.auth_service.service.UserService;
import com.nvs.auth_service.utils.JwtUtil;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService service;
    private final JwtUtil jwtUtil;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;

    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterRequest request) {

        // User user = service.save(request);
        // return service.toResponse(user);
        log.info("User registered: {}", request.getEmail());
        return ResponseEntity.ok(
                service.save(request));

    }

    @GetMapping
    public List<User> getAllUsers() {
        return service.getAll();
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        User authUser = service.login(request.getEmail(), request.getPassword());
        log.info("User logged in successfully");
        // log.error("Invalid token");
        // log.warn("Unauthorized access");
        return ResponseEntity.ok(new LoginResponse(jwtUtil.generateToken(authUser.getId(),
                authUser.getEmail(), authUser.getRole()))).getBody();

    }

}
