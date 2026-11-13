package com.example.analytics;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AnalyticsController {

    private final AnalyticsService analytics;

    AnalyticsController(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    @GetMapping("/analytics/stats")
    StatsResponse stats() {
        return new StatsResponse(analytics.totalCount(), analytics.countsByType());
    }

    record StatsResponse(long total, Map<String, Long> byType) {}
}
