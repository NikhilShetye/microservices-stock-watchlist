package com.nvs.watchlist.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nvs.watchlist.dto.request.ReorderRequest;
import com.nvs.watchlist.dto.request.WatchlistRequest;
import com.nvs.watchlist.dto.response.WatchlistResponse;
import com.nvs.watchlist.entity.Watchlist;
import com.nvs.watchlist.service.WatchlistService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public WatchlistResponse addStock(
            @RequestBody WatchlistRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();

        Watchlist watchlist = service.addStock(userId, request);
        log.info("User {} added stock {}", userId, watchlist.getStockSymbol());
        return service.toResponse(watchlist);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public Page<WatchlistResponse> getWatchlist(
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();

        return service.getUserWatchlist(userId, pageable);
    }

    // @PreAuthorize("hasRole('ADMIN')")
    // @GetMapping("/admin/watchlists")

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public void deleteStock(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        Watchlist watchlist = service.getWatchlistItem(id);
        service.deleteStock(id,userId);
        log.info("User {} deleted stock {}", userId, watchlist.getStockSymbol());
    }

    @PostMapping("/reorder")
    public void reorderWatchlist(@RequestBody ReorderRequest request, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        service.reorderWatchlist(userId, request);
    }

    

}
