package com.nvs.watchlist.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nvs.watchlist.dto.request.ReorderRequest;
import com.nvs.watchlist.dto.request.WatchlistRequest;
import com.nvs.watchlist.dto.response.WatchlistResponse;
import com.nvs.watchlist.entity.Watchlist;
import com.nvs.watchlist.service.WatchlistService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/watchlist")
@SecurityRequirement(name = "bearerAuth")
public class WatchlistController {
    private final WatchlistService service;
    private static final Logger log = LoggerFactory.getLogger(WatchlistController.class);

    public WatchlistController(WatchlistService service) {
        this.service = service;
    }

    @PostMapping
    public WatchlistResponse addStock(@RequestBody WatchlistRequest request, HttpServletRequest httpRequest) {
        Long userId = Long.parseLong(httpRequest.getHeader("X-User-Id"));
        Watchlist watchlist = service.addStock(userId,request);
        log.info("User {} added stock {}", httpRequest.getHeader("X-User-Id"), watchlist.getStockSymbol());
        return service.toResponse(watchlist);
    }

    @GetMapping
    public Page<WatchlistResponse> getWatchlist(Pageable pageable, HttpServletRequest httpRequest) {
        Long userId = Long.parseLong(httpRequest.getHeader("X-User-Id"));
        return service.getUserWatchlist(userId, pageable);
    }

    @DeleteMapping("/{id}")
    public void deleteStock(@PathVariable Long id, HttpServletRequest httpRequest) {
        Watchlist watchlist = service.getWatchlistItem(id);
        service.deleteStock(id);
        log.info("User {} deleted stock {}", httpRequest.getHeader("X-User-Id"), watchlist.getStockSymbol());
    }

    @PostMapping("/reorder")
    public void reorderWatchlist(@RequestBody ReorderRequest request, HttpServletRequest httpRequest) {
        Long userId = Long.parseLong(httpRequest.getHeader("X-User-Id"));
        service.reorderWatchlist(userId, request);
    }

    @GetMapping("/test")
    public String test(HttpServletRequest request) {

        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-Role");

        System.out.println("USER ID = " + userId);
        System.out.println("ROLE = " + role);

        return "UserId = " + userId + ", Role = " + role;
    }

}
