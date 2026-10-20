package com.analyticalrx.dataplatform.scheduler.service;

import software.amazon.awssdk.services.sqs.model.Message;

public interface SqsMessageProcessor {
    void process(Message message) throws Exception;
}
