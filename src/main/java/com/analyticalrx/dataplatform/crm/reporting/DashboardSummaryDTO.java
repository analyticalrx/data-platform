package com.analyticalrx.dataplatform.crm.reporting;

import java.util.Map;

public class DashboardSummaryDTO {

    private long totalInteractionsPeriod;
    private long followUpsDueToday;
    private long overdueFollowUps;
    private long completedFollowUpsPeriod;
    private Map<String, Long> relationshipStagesDistribution;

    public DashboardSummaryDTO() {}

    public DashboardSummaryDTO(long totalInteractionsPeriod, long followUpsDueToday, 
                               long overdueFollowUps, long completedFollowUpsPeriod, 
                               Map<String, Long> relationshipStagesDistribution) {
        this.totalInteractionsPeriod = totalInteractionsPeriod;
        this.followUpsDueToday = followUpsDueToday;
        this.overdueFollowUps = overdueFollowUps;
        this.completedFollowUpsPeriod = completedFollowUpsPeriod;
        this.relationshipStagesDistribution = relationshipStagesDistribution;
    }

    public long getTotalInteractionsPeriod() { return totalInteractionsPeriod; }
    public void setTotalInteractionsPeriod(long totalInteractionsPeriod) { this.totalInteractionsPeriod = totalInteractionsPeriod; }

    public long getFollowUpsDueToday() { return followUpsDueToday; }
    public void setFollowUpsDueToday(long followUpsDueToday) { this.followUpsDueToday = followUpsDueToday; }

    public long getOverdueFollowUps() { return overdueFollowUps; }
    public void setOverdueFollowUps(long overdueFollowUps) { this.overdueFollowUps = overdueFollowUps; }

    public long getCompletedFollowUpsPeriod() { return completedFollowUpsPeriod; }
    public void setCompletedFollowUpsPeriod(long completedFollowUpsPeriod) { this.completedFollowUpsPeriod = completedFollowUpsPeriod; }

    public Map<String, Long> getRelationshipStagesDistribution() { return relationshipStagesDistribution; }
    public void setRelationshipStagesDistribution(Map<String, Long> relationshipStagesDistribution) { 
        this.relationshipStagesDistribution = relationshipStagesDistribution; 
    }
}