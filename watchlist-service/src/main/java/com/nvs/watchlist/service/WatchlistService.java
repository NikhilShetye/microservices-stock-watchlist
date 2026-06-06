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
    public Watchlist addStock(Long userId, WatchlistRequest request) {

        // Long userId = getUserId(httpRequest);

        long count = repo.countByUserId(userId);

        Watchlist watchlist = new Watchlist();

        watchlist.setStockSymbol(request.stockSymbol);
        watchlist.setUserId(userId);
        watchlist.setPosition((int) count);

        WatchlistEvent event = WatchlistEvent.builder()
                .userId(userId)
                .symbol(request.stockSymbol)
                .action("ADDED")
                .build();

        kafkaProducerService.publishEvent(event);

        return repo.save(watchlist);
    }

    @Cacheable(value = "watchlists", key = "#userId")
    public Page<WatchlistResponse> getUserWatchlist(Long userId,
            @ParameterObject Pageable pageable) {
        log.info("Fetching watchlist from DB");

        // return repo.findByUserId(getUserId(), pageable).map(this::toResponse);
        return repo.findByUserId(userId, pageable).map(this::toResponse);
    }

    public void deleteStock(Long id) {
        repo.deleteById(id);
    }

    @Transactional
    public void reorderWatchlist(Long userId, ReorderRequest request) {
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