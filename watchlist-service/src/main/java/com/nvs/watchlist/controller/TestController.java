package com.nvs.watchlist.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/admin")
@SecurityRequirement(name = "bearerAuth")
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Admin access";
    }
}