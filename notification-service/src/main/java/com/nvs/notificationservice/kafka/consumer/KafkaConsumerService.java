package com.nvs.notificationservice.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.nvs.commonevents.WatchlistEvent;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "watchlist-events")
    public void consume(WatchlistEvent event) {
        log.info("EVENT RECEIVED: {}", event);
    }
}