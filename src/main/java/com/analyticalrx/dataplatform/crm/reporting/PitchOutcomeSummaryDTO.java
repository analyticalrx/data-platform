package com.analyticalrx.dataplatform.crm.reporting;

import java.util.Map;
import java.util.UUID;

public class PitchOutcomeSummaryDTO {

    private UUID pitchId;
    private String pitchName;
    private Long totalUsage;
    private Map<String, Long> outcomesCount;

    public PitchOutcomeSummaryDTO() {}

    public PitchOutcomeSummaryDTO(UUID pitchId, String pitchName, Long totalUsage, Map<String, Long> outcomesCount) {
        this.pitchId = pitchId;
        this.pitchName = pitchName;
        this.totalUsage = totalUsage;
        this.outcomesCount = outcomesCount;
    }

    public UUID getPitchId() { return pitchId; }
    public void setPitchId(UUID pitchId) { this.pitchId = pitchId; }

    public String getPitchName() { return pitchName; }
    public void setPitchName(String pitchName) { this.pitchName = pitchName; }

    public Long getTotalUsage() { return totalUsage; }
    public void setTotalUsage(Long totalUsage) { this.totalUsage = totalUsage; }

    public Map<String, Long> getOutcomesCount() { return outcomesCount; }
    public void setOutcomesCount(Map<String, Long> outcomesCount) { this.outcomesCount = outcomesCount; }
}