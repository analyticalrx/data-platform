package com.analyticalrx.dataplatform.scheduler.service.impl;

import com.analyticalrx.dataplatform.scheduler.dto.EventEnvelope;
import com.analyticalrx.dataplatform.scheduler.service.IngestionPipelineProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IngestionPipelineProcessingServiceImpl implements IngestionPipelineProcessingService {

    @Override
    public void process(EventEnvelope event) {

    }
}
