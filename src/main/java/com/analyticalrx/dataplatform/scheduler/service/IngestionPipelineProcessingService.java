package com.analyticalrx.dataplatform.scheduler.service;

import com.analyticalrx.dataplatform.scheduler.dto.EventEnvelope;

public interface IngestionPipelineProcessingService {

    void process(EventEnvelope event);
}
