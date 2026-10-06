package com.analyticalrx.dataplatform.dashboard.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.security.PermissionGuard;
import com.analyticalrx.dataplatform.dashboard.model.DashboardResponse;
import com.analyticalrx.dataplatform.dashboard.repository.DashboardRepository;
import com.analyticalrx.dataplatform.dashboard.repository.DashboardRepository.StoreActivity;
import com.analyticalrx.dataplatform.dashboard.repository.DashboardRepository.TaskCounts;
import com.analyticalrx.dataplatform.pharmacy.model.PharmacyResponse;
import com.analyticalrx.dataplatform.pharmacy.repository.PharmacyRepository;

@Service
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PermissionGuard permissionGuard;

    public DashboardService(DashboardRepository dashboardRepository, PharmacyRepository pharmacyRepository,
                            PermissionGuard permissionGuard) {
        this.dashboardRepository = dashboardRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.permissionGuard = permissionGuard;
    }

    @Transactional(readOnly = true)
    public DashboardResponse owner(PlatformUserDetails user, LocalDate startDate, LocalDate endDate,
                                   String pharmacyKey) {
        permissionGuard.require(user, "dashboard.owner.read");
        return build("owner", user, startDate, endDate, pharmacyKey);
    }

    @Transactional(readOnly = true)
    public DashboardResponse storeManager(PlatformUserDetails user, LocalDate startDate, LocalDate endDate,
                                          String pharmacyKey) {
        permissionGuard.require(user, "dashboard.store_manager.read");
        return build("store-manager", user, startDate, endDate, pharmacyKey);
    }

    private DashboardResponse build(String dashboardId, PlatformUserDetails user, LocalDate startDate,
                                    LocalDate endDate, String pharmacyKey) {
        LocalDate end = endDate == null ? LocalDate.now(ZoneOffset.UTC) : endDate;
        LocalDate start = startDate == null ? end.minusDays(29) : startDate;
        if (start.isAfter(end) || start.isBefore(end.minusDays(365))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date range must be 1-366 days");
        }

        List<PharmacyResponse> pharmacies = pharmacyRepository.findAccessible(user);
        if (pharmacyKey != null) {
            pharmacies = pharmacies.stream().filter(p -> p.pharmacyKey().equals(pharmacyKey)).toList();
            if (pharmacies.isEmpty()) {
                // A scoped list does not disclose whether the pharmacy exists in another tenant.
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found");
            }
        }

        List<String> keys = pharmacies.stream().map(PharmacyResponse::pharmacyKey).toList();
        Map<String, StoreActivity> activity = keys.isEmpty() ? Map.of()
                : dashboardRepository.activityByStore(user, keys, start, end);
        Map<String, TaskCounts> tasks = keys.isEmpty() ? Map.of()
                : dashboardRepository.taskCountsByStore(user, keys);
        List<DashboardResponse.TrendPoint> trend = keys.isEmpty() ? List.of()
                : dashboardRepository.eventTrend(user, keys, start, end);

        long eventCount = 0, billedCount = 0, rejectedCount = 0, reversedCount = 0;
        long openTasks = 0, overdueTasks = 0;
        boolean partial = false;
        LocalDate latestBusinessDate = null;
        Instant latestImportedAt = null;
        List<DashboardResponse.Store> stores = new ArrayList<>();

        for (PharmacyResponse pharmacy : pharmacies) {
            StoreActivity a = activity.get(pharmacy.pharmacyKey());
            TaskCounts t = tasks.getOrDefault(pharmacy.pharmacyKey(), new TaskCounts(0, 0));
            openTasks += t.openCount();
            overdueTasks += t.overdueCount();
            String status = "NO_DATA";
            DashboardResponse.Metrics metrics = new DashboardResponse.Metrics(null, null, null, null,
                    t.openCount(), t.overdueCount());
            if (a != null) {
                eventCount += a.eventCount();
                billedCount += a.billedEventCount();
                rejectedCount += a.rejectedEventCount();
                reversedCount += a.reversedEventCount();
                partial |= a.partialEventCount() > 0;
                status = a.partialEventCount() > 0 ? "PARTIAL" : "AVAILABLE";
                metrics = new DashboardResponse.Metrics(a.eventCount(), a.billedEventCount(),
                        a.rejectedEventCount(), a.reversedEventCount(), t.openCount(), t.overdueCount());
                if (latestBusinessDate == null || a.latestBusinessDate().isAfter(latestBusinessDate)) {
                    latestBusinessDate = a.latestBusinessDate();
                }
                if (latestImportedAt == null || a.latestImportedAt().isAfter(latestImportedAt)) {
                    latestImportedAt = a.latestImportedAt();
                }
            }
            stores.add(new DashboardResponse.Store(pharmacy.pharmacyKey(), pharmacy.displayName(), status, metrics));
        }

        String dataStatus = activity.isEmpty() ? "NO_DATA"
                : partial || activity.size() < pharmacies.size() ? "PARTIAL" : "AVAILABLE";
        DashboardResponse.Metrics totals = new DashboardResponse.Metrics(
                activity.isEmpty() ? null : eventCount,
                activity.isEmpty() ? null : billedCount,
                activity.isEmpty() ? null : rejectedCount,
                activity.isEmpty() ? null : reversedCount,
                openTasks, overdueTasks);
        return new DashboardResponse(dashboardId, Instant.now(),
                new DashboardResponse.Filters(start, end, pharmacyKey), dataStatus,
                latestBusinessDate, latestImportedAt, totals, stores, trend);
    }
}
