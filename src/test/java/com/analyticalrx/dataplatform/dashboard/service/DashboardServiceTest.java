package com.analyticalrx.dataplatform.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.security.PermissionGuard;
import com.analyticalrx.dataplatform.dashboard.repository.DashboardRepository;
import com.analyticalrx.dataplatform.dashboard.repository.DashboardRepository.TaskCounts;
import com.analyticalrx.dataplatform.dashboard.repository.DashboardRepository.StoreActivity;
import com.analyticalrx.dataplatform.pharmacy.model.PharmacyResponse;
import com.analyticalrx.dataplatform.pharmacy.repository.PharmacyRepository;

class DashboardServiceTest {
    private DashboardRepository dashboardRepository;
    private PharmacyRepository pharmacyRepository;
    private DashboardService service;
    private final LocalDate date = LocalDate.of(2026, 10, 5);

    @BeforeEach
    void setUp() {
        dashboardRepository = mock(DashboardRepository.class);
        pharmacyRepository = mock(PharmacyRepository.class);
        service = new DashboardService(dashboardRepository, pharmacyRepository, new PermissionGuard());
    }

    @Test
    void noEventsDoNotBecomeZeroActivity() {
        var owner = user("OWNER", "dashboard.owner.read");
        var pharmacy = new PharmacyResponse("PHARM-001", "Demo Pharmacy", "NY", null, null);
        when(pharmacyRepository.findAccessible(owner)).thenReturn(List.of(pharmacy));
        when(dashboardRepository.taskCountsByStore(owner, List.of("PHARM-001")))
                .thenReturn(Map.of("PHARM-001", new TaskCounts(2, 1)));

        var result = service.owner(owner, date, date, null);

        assertEquals("NO_DATA", result.dataStatus());
        assertNull(result.totals().eventCount());
        assertEquals(2, result.totals().openTaskCount());
        assertEquals(1, result.totals().overdueTaskCount());
        assertEquals("NO_DATA", result.stores().getFirst().dataStatus());
    }

    @Test
    void managerCannotOpenOwnerDashboard() {
        var manager = user("STORE_MANAGER", "dashboard.store_manager.read");
        assertThrows(AccessDeniedException.class, () -> service.owner(manager, date, date, null));
    }

    @Test
    void oneStoreWithoutEventsMakesCompanyDataPartial() {
        var owner = user("OWNER", "dashboard.owner.read");
        when(pharmacyRepository.findAccessible(owner)).thenReturn(List.of(
                new PharmacyResponse("PHARM-001", "First", "NY", null, null),
                new PharmacyResponse("PHARM-002", "Second", "NY", null, null)));
        when(dashboardRepository.activityByStore(owner, List.of("PHARM-001", "PHARM-002"), date, date))
                .thenReturn(Map.of("PHARM-001", new StoreActivity("PHARM-001", 3, 1, 1, 0,
                        0, date, Instant.parse("2026-10-05T12:00:00Z"))));

        var result = service.owner(owner, date, date, null);

        assertEquals("PARTIAL", result.dataStatus());
        assertEquals(3, result.totals().eventCount());
        assertNull(result.stores().get(1).metrics().eventCount());
    }

    @Test
    void managerCannotSelectUnassignedPharmacy() {
        var manager = user("STORE_MANAGER", "dashboard.store_manager.read");
        when(pharmacyRepository.findAccessible(manager)).thenReturn(List.of(
                new PharmacyResponse("PHARM-001", "Demo Pharmacy", "NY", null, null)));

        var error = assertThrows(ResponseStatusException.class,
                () -> service.storeManager(manager, date, date, "PHARM-002"));
        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void rejectsReversedDateRange() {
        var owner = user("OWNER", "dashboard.owner.read");
        var error = assertThrows(ResponseStatusException.class,
                () -> service.owner(owner, date, date.minusDays(1), null));
        assertEquals(400, error.getStatusCode().value());
    }

    private PlatformUserDetails user(String role, String permission) {
        return new PlatformUserDetails(1, 1, "test@example.test", "Test", "", "active", 1,
                Set.of(role), Set.of(permission), Set.of());
    }
}
