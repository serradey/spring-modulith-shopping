package com.example.analytics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically reports how many events were processed per second over the last interval. */
@Component
class ThroughputReporter {

    private static final Logger log = LoggerFactory.getLogger(ThroughputReporter.class);
    private static final long INTERVAL_MS = 10_000;

    private final AtomicLong windowCount = new AtomicLong();
    private final Counter consumed;

    ThroughputReporter(MeterRegistry registry) {
        this.consumed = Counter.builder("analytics.events.consumed")
                .description("Total cart events consumed by analytics")
                .register(registry);
    }

    void tick() {
        windowCount.incrementAndGet();
        consumed.increment();
    }

    @Scheduled(fixedRate = INTERVAL_MS)
    void report() {
        long count = windowCount.getAndSet(0);
        if (count > 0) {
            double perSecond = (double) count / (INTERVAL_MS / 1000.0);
            log.info("Analytics throughput: {} events ({} events/s) over the last {} ms", count, perSecond, INTERVAL_MS);
        }
    }
}
