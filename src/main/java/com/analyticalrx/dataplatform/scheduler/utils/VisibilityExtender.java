package com.analyticalrx.dataplatform.scheduler.utils;

import com.analyticalrx.dataplatform.scheduler.config.SqsBatchProperties;
import com.analyticalrx.dataplatform.scheduler.utils.contract.Lease;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisibilityExtender {

    private final SqsClient sqsClient;
    private final SqsBatchProperties properties;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, Thread.ofPlatform().name("sqs-visibility-", 0).factory());

    public Lease start(Message message) {

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> extend(message),
                properties.visibilityHeartbeatSeconds(),
                properties.visibilityHeartbeatSeconds(),
                TimeUnit.SECONDS
        );

        return () -> future.cancel(false);
    }

    private void extend(Message message) {
        try {
            sqsClient.changeMessageVisibility(ChangeMessageVisibilityRequest.builder()
                    .queueUrl(properties.queueUrl())
                    .receiptHandle(message.receiptHandle())
                    .visibilityTimeout(properties.visibilityTimeoutSeconds())
                    .build());
            log.debug("Extended visibility. messageId={}", message.messageId());
        } catch (Exception e) {
            /*
             * Don't abort business processing.
             * Idempotency remains the last line
             * of protection if visibility expires.
             */
            log.error("Unable to extend visibility. " + "messageId={}", message.messageId(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }

}