package com.analyticalrx.dataplatform.scheduler.pojo;

import software.amazon.awssdk.services.sqs.model.Message;

public record ProcessingResult(Message message, boolean successful) {

    public static ProcessingResult success(Message message) {
        return new ProcessingResult(message, true);
    }

    public static ProcessingResult failure(Message message) {
        return new ProcessingResult(message, false);
    }
}