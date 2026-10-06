package com.analyticalrx.dataplatform.dashboard.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.dashboard.model.DashboardResponse;
import com.analyticalrx.dataplatform.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/v1/dashboards")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/owner")
    public DashboardResponse owner(@AuthenticationPrincipal PlatformUserDetails user,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   @RequestParam(required = false) String pharmacyKey) {
        return dashboardService.owner(user, startDate, endDate, pharmacyKey);
    }

    @GetMapping("/store-manager")
    public DashboardResponse storeManager(@AuthenticationPrincipal PlatformUserDetails user,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                          @RequestParam(required = false) String pharmacyKey) {
        return dashboardService.storeManager(user, startDate, endDate, pharmacyKey);
    }
}
