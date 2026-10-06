package com.analyticalrx.dataplatform.dashboard.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        String dashboardId,
        Instant generatedAt,
        Filters filters,
        String dataStatus,
        LocalDate latestBusinessDate,
        Instant latestImportedAt,
        Metrics totals,
        List<Store> stores,
        List<TrendPoint> trend
) {
    public record Filters(LocalDate startDate, LocalDate endDate, String pharmacyKey) {}

    public record Metrics(Long eventCount, Long billedEventCount, Long rejectedEventCount,
                          Long reversedEventCount, long openTaskCount, long overdueTaskCount) {}

    public record Store(String pharmacyKey, String displayName, String dataStatus,
                        Metrics metrics) {}

    public record TrendPoint(LocalDate date, long eventCount) {}
}
