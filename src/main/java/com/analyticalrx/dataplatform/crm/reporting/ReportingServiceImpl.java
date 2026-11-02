package com.analyticalrx.dataplatform.crm.reporting;

import com.analyticalrx.dataplatform.crm.doctor_office.DoctorOffice;
import com.analyticalrx.dataplatform.crm.doctor_office.DoctorOfficeRepository;
import com.analyticalrx.dataplatform.crm.followup.FollowUpTask;
import com.analyticalrx.dataplatform.crm.followup.FollowUpTaskRepository;
import com.analyticalrx.dataplatform.crm.followup.TaskStatus;
import com.analyticalrx.dataplatform.crm.interaction.Interaction;
import com.analyticalrx.dataplatform.crm.interaction.InteractionRepository;
import com.analyticalrx.dataplatform.crm.interaction.Pitch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportingServiceImpl implements ReportingService {

    private final InteractionRepository interactionRepository;
    private final FollowUpTaskRepository taskRepository;
    private final DoctorOfficeRepository officeRepository;
    private final PitchRepository pitchRepository;

    @Autowired
    public ReportingServiceImpl(InteractionRepository interactionRepository,
                                FollowUpTaskRepository taskRepository,
                                DoctorOfficeRepository officeRepository,
                                PitchRepository pitchRepository) {
        this.interactionRepository = interactionRepository;
        this.taskRepository = taskRepository;
        this.officeRepository = officeRepository;
        this.pitchRepository = pitchRepository;
    }

    @Override
    public List<PitchOutcomeSummaryDTO> getPitchOutcomesReport() {
        List<Pitch> activePitches = pitchRepository.findByActiveTrue();
        List<Interaction> allInteractions = interactionRepository.findAll();

        List<PitchOutcomeSummaryDTO> reports = new ArrayList<>();

        for (Pitch pitch : activePitches) {
            List<Interaction> pitchInteractions = allInteractions.stream()
                    .filter(i -> pitch.getId().equals(i.getPitchId()))
                    .collect(Collectors.toList());

            Map<String, Long> outcomeCount = pitchInteractions.stream()
                    .collect(Collectors.groupingBy(i -> i.getOutcome().name(), Collectors.counting()));

            reports.add(new PitchOutcomeSummaryDTO(
                    pitch.getId(),
                    pitch.getName(),
                    (long) pitchInteractions.size(),
                    outcomeCount
            ));
        }

        return reports;
    }

    @Override
    public DashboardSummaryDTO getDashboardSummary(Instant startDate, Instant endDate) {
        Instant now = Instant.now();
        Instant start = (startDate != null) ? startDate : now.minus(30, ChronoUnit.DAYS);
        Instant end = (endDate != null) ? endDate : now;

        // 1. Interactions in period
        long interactionsCount = interactionRepository.findAll().stream()
                .filter(i -> !i.getOccurredAt().isBefore(start) && !i.getOccurredAt().isAfter(end))
                .count();

        // 2. Tasks
        List<FollowUpTask> allTasks = taskRepository.findAll();

        Instant startOfToday = now.truncatedTo(ChronoUnit.DAYS);
        Instant endOfToday = startOfToday.plus(1, ChronoUnit.DAYS);

        long dueTodayCount = allTasks.stream()
                .filter(t -> (t.getStatus() == TaskStatus.OPEN || t.getStatus() == TaskStatus.IN_PROGRESS)
                        && !t.getDueAt().isBefore(startOfToday) && t.getDueAt().isBefore(endOfToday))
                .count();

        long overdueCount = allTasks.stream()
                .filter(t -> (t.getStatus() == TaskStatus.OPEN || t.getStatus() == TaskStatus.IN_PROGRESS)
                        && t.getDueAt().isBefore(now))
                .count();

        long completedPeriodCount = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED
                        && t.getCompletedAt() != null
                        && !t.getCompletedAt().isBefore(start) && !t.getCompletedAt().isAfter(end))
                .count();

        // 3. Doctor Office Relationship Stage distribution
        Map<String, Long> stageDistribution = officeRepository.findAll().stream()
                .collect(Collectors.groupingBy(o -> o.getRelationshipStage().name(), Collectors.counting()));

        return new DashboardSummaryDTO(
                interactionsCount,
                dueTodayCount,
                overdueCount,
                completedPeriodCount,
                stageDistribution
        );
    }
}