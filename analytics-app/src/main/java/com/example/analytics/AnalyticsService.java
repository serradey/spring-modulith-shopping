package com.example.analytics;

import com.example.commerce.events.CartEventMessage;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records consumed cart events. Idempotent: an event whose id has already been processed is ignored,
 * so duplicate delivery (at-least-once) does not corrupt the counts.
 */
@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final ProcessedEventRepository processed;
    private final EventCountRepository counts;
    private final ThroughputReporter throughput;

    AnalyticsService(
            ProcessedEventRepository processed, EventCountRepository counts, ThroughputReporter throughput) {
        this.processed = processed;
        this.counts = counts;
        this.throughput = throughput;
    }

    @Transactional
    public void record(CartEventMessage event) {
        if (processed.existsById(event.eventId())) {
            log.debug("Duplicate event {} ignored.", event.eventId());
            return;
        }

        EventCount counter = counts.findById(event.eventType())
                .orElseGet(() -> new EventCount(event.eventType(), 0L));
        counter.increment();
        counts.save(counter);

        processed.save(new ProcessedEvent(event.eventId(), Instant.now()));
        throughput.tick();

        log.info("Consumed {} for cart {}.", event.eventType(), event.cartId());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> countsByType() {
        Map<String, Long> result = new LinkedHashMap<>();
        counts.findAll().forEach(c -> result.put(c.eventType(), c.count()));
        return result;
    }

    @Transactional(readOnly = true)
    public long totalCount() {
        return counts.findAll().stream().mapToLong(EventCount::count).sum();
    }
}
