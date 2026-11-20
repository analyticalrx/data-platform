package com.analyticalrx.dataplatform.crm.reporting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController("crmDashboardController")
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final ReportingService reportingService;

    @Autowired
    public DashboardController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        return ResponseEntity.ok(reportingService.getDashboardSummary(startDate, endDate));
    }
}