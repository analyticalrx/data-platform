package com.analyticalrx.dataplatform.crm.reporting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class PitchOutcomeReportController {

    private final ReportingService reportingService;

    @Autowired
    public PitchOutcomeReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/pitch-outcomes")
    public ResponseEntity<List<PitchOutcomeSummaryDTO>> getPitchOutcomes() {
        return ResponseEntity.ok(reportingService.getPitchOutcomesReport());
    }
}