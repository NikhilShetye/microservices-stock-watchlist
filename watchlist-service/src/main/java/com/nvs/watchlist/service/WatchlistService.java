package com.nvs.watchlist.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.nvs.commonevents.WatchlistEvent;
import com.nvs.watchlist.dto.request.ReorderRequest;
import com.nvs.watchlist.dto.request.WatchlistRequest;
import com.nvs.watchlist.dto.response.WatchlistResponse;
import com.nvs.watchlist.entity.Watchlist;
import com.nvs.watchlist.kafka.producer.KafkaProducerService;
import com.nvs.watchlist.repository.WatchlistRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository repo;
    private final KafkaProducerService kafkaProducerService;

    private static final Logger log = LoggerFactory.getLogger(WatchlistService.class);

    @CacheEvict(value = "watchlists", key = "#userId")
    public Watchlist addStock(String userId, WatchlistRequest request) {

        long count = repo.countByUserId(userId);

        Watchlist watchlist = new Watchlist();
        watchlist.setStockSymbol(request.stockSymbol);
        watchlist.setUserId(userId);
        watchlist.setPosition((int) count);

        // 1. SAVE FIRST (critical business operation)
        Watchlist saved = repo.save(watchlist);

        // 2. THEN publish event (non-critical)
        WatchlistEvent event = WatchlistEvent.builder()
                .userId(userId)
                .symbol(request.stockSymbol)
                .action("ADDED")
                .build();

        try {
            kafkaProducerService.publishEvent(event);
        } catch (Exception e) {
            log.error("Kafka failed but DB already saved", e);
        }

        return saved;
    }

    @Cacheable(value = "watchlists", key = "#userId")
    public Page<WatchlistResponse> getUserWatchlist(String userId,
            @ParameterObject Pageable pageable) {
        log.info("Fetching watchlist from DB");

        // return repo.findByUserId(getUserId(), pageable).map(this::toResponse);
        return repo.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Transactional
    @CacheEvict(value = "watchlists", key = "#userId")
    public void deleteStock(Long id, String userId) {
        Watchlist watchlist = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        if (!watchlist.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        repo.delete(watchlist);
    }

    @Transactional
    @CacheEvict(value = "watchlists", key = "#userId")
    public void reorderWatchlist(String userId, ReorderRequest request) {
        // Long userId = getUserId(httpRequest);

        List<Watchlist> list = repo.findByUserIdOrderByPosition(userId);

        Watchlist movedItem = list.stream()
                .filter(w -> w.getId().equals(request.id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        int oldPosition = movedItem.getPosition();
        int newPosition = request.newPosition;

        if (oldPosition == newPosition) {
            return; // No change needed
        }

        for (Watchlist w : list) {
            if (oldPosition < newPosition) {
                // Moving down
                if (w.getPosition() > oldPosition && w.getPosition() <= newPosition) {
                    w.setPosition(w.getPosition() - 1);
                }
            } else {
                // Moving up
                if (w.getPosition() < oldPosition && w.getPosition() >= newPosition) {
                    w.setPosition(w.getPosition() + 1);
                }
            }
        }

        movedItem.setPosition(newPosition);
        repo.saveAll(list);
    }

    public WatchlistResponse toResponse(Watchlist w) {
        WatchlistResponse res = new WatchlistResponse();
        res.id = w.getId();
        res.stockSymbol = w.getStockSymbol();
        res.position = w.getPosition();
        return res;
    }

    public Watchlist getWatchlistItem(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
    }
}