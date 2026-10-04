package com.analyticalrx.dataplatform.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.sqs")
public record SqsBatchProperties(String queueUrl,
        long pollIntervalMs,
        int maxMessagesPerRun,
        int receiveBatchSize,
        int receiveWaitSeconds,
        int visibilityTimeoutSeconds,
        int visibilityHeartbeatSeconds,
        int processingConcurrency,
        Duration maxRunDuration
) {
}