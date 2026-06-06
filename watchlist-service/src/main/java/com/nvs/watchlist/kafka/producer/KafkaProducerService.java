package com.nvs.watchlist.kafka.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.nvs.commonevents.WatchlistEvent;

@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, WatchlistEvent> kafkaTemplate;

    // @Autowired
    // private KafkaTemplate<String, String> stringKafkaTemplate;

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    public void publishEvent(WatchlistEvent event) {

        // stringKafkaTemplate.send("watchlist-events", "HELLO");

        kafkaTemplate.send("watchlist-events", event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka send failed", ex);
                    } else {
                        log.info("Kafka send SUCCESS. Topic={}, Partition={}, Offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}