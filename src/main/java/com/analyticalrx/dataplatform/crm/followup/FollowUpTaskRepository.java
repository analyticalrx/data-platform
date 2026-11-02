package com.analyticalrx.dataplatform.crm.followup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface FollowUpTaskRepository extends JpaRepository<FollowUpTask, UUID> {

    List<FollowUpTask> findByAssignedToAndStatusIn(UUID assignedTo, List<TaskStatus> statuses);

    List<FollowUpTask> findByOfficeId(UUID officeId);

    // Overdue tasks query defined in TDD Section 13
    @Query("SELECT t FROM FollowUpTask t WHERE t.assignedTo = :assignedTo AND t.status IN :statuses AND t.dueAt < :now ORDER BY t.dueAt ASC")
    List<FollowUpTask> findOverdueTasks(
            @Param("assignedTo") UUID assignedTo,
            @Param("statuses") List<TaskStatus> statuses,
            @Param("now") Instant now
    );

    // Calendar view query by date range
    List<FollowUpTask> findByDueAtBetween(Instant start, Instant end);
}