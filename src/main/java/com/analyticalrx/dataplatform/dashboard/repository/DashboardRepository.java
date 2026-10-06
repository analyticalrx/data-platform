package com.analyticalrx.dataplatform.dashboard.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.analyticalrx.dataplatform.auth.access.TenantSchemaResolver;
import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.dashboard.model.DashboardResponse.TrendPoint;

@Repository
public class DashboardRepository {
    public record StoreActivity(String pharmacyKey, long eventCount, long billedEventCount,
                                long rejectedEventCount, long reversedEventCount,
                                long partialEventCount, LocalDate latestBusinessDate,
                                Instant latestImportedAt) {}

    public record TaskCounts(long openCount, long overdueCount) {}

    private final NamedParameterJdbcTemplate jdbc;
    private final TenantSchemaResolver schemaResolver;

    public DashboardRepository(NamedParameterJdbcTemplate jdbc, TenantSchemaResolver schemaResolver) {
        this.jdbc = jdbc;
        this.schemaResolver = schemaResolver;
    }

    public Map<String, StoreActivity> activityByStore(PlatformUserDetails user, List<String> pharmacyKeys,
                                                      LocalDate startDate, LocalDate endDate) {
        String schema = schemaResolver.findActiveSchemaName(user);
        String sql = """
                select p.pharmacy_key, count(*) as event_count,
                       count(*) filter (where e.canonical_event_type = 'claim_billed') as billed_count,
                       count(*) filter (where e.canonical_event_type = 'claim_rejected') as rejected_count,
                       count(*) filter (where e.canonical_event_type = 'claim_reversed') as reversed_count,
                       count(*) filter (where e.data_quality_status <> 'valid') as partial_count,
                       max(e.business_date) as latest_business_date,
                       max(e.created_at) as latest_imported_at
                from %s.pharmacy_event e
                join %s.pharmacy p on p.id = e.pharmacy_id and p.tenant_id = e.tenant_id
                where e.tenant_id = :tenantId and p.status = 'active'
                  and p.pharmacy_key in (:pharmacyKeys)
                  and e.business_date between :startDate and :endDate
                group by p.pharmacy_key
                """.formatted(schema, schema);
        return jdbc.query(sql, params(user, pharmacyKeys, startDate, endDate),
                (rs, row) -> new StoreActivity(rs.getString("pharmacy_key"), rs.getLong("event_count"),
                        rs.getLong("billed_count"), rs.getLong("rejected_count"),
                        rs.getLong("reversed_count"), rs.getLong("partial_count"),
                        rs.getObject("latest_business_date", LocalDate.class),
                        rs.getObject("latest_imported_at", java.time.OffsetDateTime.class).toInstant()))
                .stream().collect(Collectors.toMap(StoreActivity::pharmacyKey, Function.identity()));
    }

    public List<TrendPoint> eventTrend(PlatformUserDetails user, List<String> pharmacyKeys,
                                       LocalDate startDate, LocalDate endDate) {
        String schema = schemaResolver.findActiveSchemaName(user);
        String sql = """
                select e.business_date, count(*) as event_count
                from %s.pharmacy_event e
                join %s.pharmacy p on p.id = e.pharmacy_id and p.tenant_id = e.tenant_id
                where e.tenant_id = :tenantId and p.status = 'active'
                  and p.pharmacy_key in (:pharmacyKeys)
                  and e.business_date between :startDate and :endDate
                group by e.business_date order by e.business_date
                """.formatted(schema, schema);
        return jdbc.query(sql, params(user, pharmacyKeys, startDate, endDate),
                (rs, row) -> new TrendPoint(rs.getObject("business_date", LocalDate.class),
                        rs.getLong("event_count")));
    }

    public Map<String, TaskCounts> taskCountsByStore(PlatformUserDetails user, List<String> pharmacyKeys) {
        String schema = schemaResolver.findActiveSchemaName(user);
        String sql = """
                select pharmacy_key, count(*) as open_count,
                       count(*) filter (where due_at < now()) as overdue_count
                from %s.task
                where tenant_id = :tenantId and pharmacy_key in (:pharmacyKeys)
                  and status not in ('COMPLETED', 'SKIPPED', 'CANCELED')
                group by pharmacy_key
                """.formatted(schema);
        var params = new MapSqlParameterSource()
                .addValue("tenantId", user.tenantId())
                .addValue("pharmacyKeys", pharmacyKeys);
        return jdbc.query(sql, params, (rs, row) -> Map.entry(rs.getString("pharmacy_key"),
                        new TaskCounts(rs.getLong("open_count"), rs.getLong("overdue_count"))))
                .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private MapSqlParameterSource params(PlatformUserDetails user, List<String> pharmacyKeys,
                                         LocalDate startDate, LocalDate endDate) {
        return new MapSqlParameterSource()
                .addValue("tenantId", user.tenantId())
                .addValue("pharmacyKeys", pharmacyKeys)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
    }
}
