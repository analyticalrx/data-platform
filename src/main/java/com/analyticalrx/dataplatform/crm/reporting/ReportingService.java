package com.analyticalrx.dataplatform.crm.reporting;

import java.time.Instant;
import java.util.List;

public interface ReportingService {
    List<PitchOutcomeSummaryDTO> getPitchOutcomesReport();
    DashboardSummaryDTO getDashboardSummary(Instant startDate, Instant endDate);
}