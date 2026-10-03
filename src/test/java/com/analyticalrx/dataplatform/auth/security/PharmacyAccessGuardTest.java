package com.analyticalrx.dataplatform.auth.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.repository.PharmacyAccessRepository;

class PharmacyAccessGuardTest {
    private final StubPharmacyAccessRepository repository = new StubPharmacyAccessRepository();
    private final PharmacyAccessGuard guard = new PharmacyAccessGuard(repository);

    @Test
    void ownerAndTenantAdminHaveTenantWideMetadataAccess() {
        assertDoesNotThrow(() -> guard.requirePharmacyAccess(user("OWNER"), "PHARM-003"));
        assertDoesNotThrow(() -> guard.requirePharmacyAccess(user("TENANT_ADMIN"), "PHARM-003"));
        assertEquals(0, repository.calls);
    }

    @Test
    void assignedUsersCanReadTheirPharmaciesRegardlessOfBusinessRole() {
        repository.assigned = true;

        for (String role : List.of("STORE_MANAGER", "BOOKKEEPER", "TECHNICIAN")) {
            assertDoesNotThrow(() -> guard.requirePharmacyAccess(user(role), "PHARM-001"));
        }

        assertEquals(3, repository.calls);
    }

    @Test
    void unassignedPharmacyIsDenied() {
        assertThrows(AccessDeniedException.class,
                () -> guard.requirePharmacyAccess(user("STORE_MANAGER"), "PHARM-003"));
    }

    @Test
    void inactiveOrRolelessUserIsDenied() {
        assertThrows(AccessDeniedException.class,
                () -> guard.requirePharmacyAccess(user("OWNER", "inactive"), "PHARM-001"));
        assertThrows(AccessDeniedException.class,
                () -> guard.requirePharmacyAccess(user(null), "PHARM-001"));
        assertEquals(0, repository.calls);
    }

    private PlatformUserDetails user(String role) {
        return user(role, "active");
    }

    private PlatformUserDetails user(String role, String status) {
        return new PlatformUserDetails(5L, 1L, "test@analyticalrx.dev", "Test User", "hash", status, 1,
                role == null ? Set.of() : Set.of(role), Set.of(), List.of());
    }

    private static class StubPharmacyAccessRepository extends PharmacyAccessRepository {
        private boolean assigned;
        private int calls;

        StubPharmacyAccessRepository() {
            super(new JdbcTemplate());
        }

        @Override
        public boolean hasActiveAssignment(long userId, long tenantId, String pharmacyKey) {
            calls++;
            return assigned && userId == 5L && tenantId == 1L && pharmacyKey.equals("PHARM-001");
        }
    }
}
