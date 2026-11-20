package com.analyticalrx.dataplatform.crm.followup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar")
public class CalendarController {

    private final FollowUpTaskService taskService;

    @Autowired
    public CalendarController(FollowUpTaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/activities")
    public ResponseEntity<List<FollowUpTaskResponseDTO>> getCalendarActivities(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        return ResponseEntity.ok(taskService.getCalendarActivities(start, end));
    }
}