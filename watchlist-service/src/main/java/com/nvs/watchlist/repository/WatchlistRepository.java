package com.nvs.watchlist.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nvs.watchlist.entity.Watchlist;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUserIdOrderByPosition(String userId);

    Page<Watchlist> findByUserId(String userId, Pageable pageable);

    long countByUserId(String userId);

}
