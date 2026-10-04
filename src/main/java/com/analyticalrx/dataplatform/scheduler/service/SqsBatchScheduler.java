package com.analyticalrx.dataplatform.scheduler.service;

import com.analyticalrx.dataplatform.scheduler.config.SqsBatchProperties;
import com.analyticalrx.dataplatform.scheduler.pojo.ProcessingResult;
import com.analyticalrx.dataplatform.scheduler.utils.VisibilityExtender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsBatchScheduler {

    private final SqsClient sqsClient;
    private final SqsBatchProperties properties;
    private final SqsMessageProcessor messageProcessor;
    private final VisibilityExtender visibilityExtender;
    @Qualifier("sqsProcessingExecutor")
    private final TaskExecutor taskExecutor;

    /*
     * Protect against accidental overlapping executions
     * WITHIN THIS EC2 INSTANCE.
     *
     * This is intentionally NOT a distributed lock.
     */
    private final AtomicBoolean jobRunning = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${app.sqs.poll-interval-ms:60000}")
    public void poll() {
        if (!jobRunning.compareAndSet(false, true)) {
            log.warn("Previous SQS batch still running. " + "Skipping this execution.");
            return;
        }
        try {
            runBatch();
        } catch (Exception e) {
            /*
             * Do not kill scheduler thread.
             * Next scheduled invocation becomes fallback.
             */

            log.error(
                    "Unexpected failure during SQS batch run",
                    e
            );

        } finally {

            jobRunning.set(false);
        }
    }

    private void runBatch() {

        Instant deadline = Instant.now().plus(properties.maxRunDuration());

        int receivedCount = 0;

        log.info("Starting SQS batch run. maxMessages={}", properties.maxMessagesPerRun());

        while (receivedCount < properties.maxMessagesPerRun() && Instant.now().isBefore(deadline)) {
            int remaining = properties.maxMessagesPerRun() - receivedCount;

            int receiveCount = Math.min(Math.min(properties.receiveBatchSize(), remaining), 10);

            List<Message> messages = receiveMessages(receiveCount);

            if (messages.isEmpty()) {
                log.debug("No additional messages available");
                break;
            }

            receivedCount += messages.size();

            processBatch(messages);
        }

        log.info("Completed SQS batch run. received={}", receivedCount);
    }

    private List<Message> receiveMessages(int maxMessages) {

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .maxNumberOfMessages(maxMessages)
                .waitTimeSeconds(properties.receiveWaitSeconds())
                .visibilityTimeout(properties.visibilityTimeoutSeconds())
                .messageSystemAttributeNames(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT)
                .build();

        return sqsClient
                .receiveMessage(request)
                .messages();
    }

    private void processBatch(List<Message> messages) {

        List<CompletableFuture<ProcessingResult>> futures = messages.stream().map(message ->
                        CompletableFuture.supplyAsync(() -> processOneMessage(message), taskExecutor))
                        .toList();

        List<ProcessingResult> results = futures.stream().map(CompletableFuture::join).toList();

        List<Message> successfulMessages = results.stream().filter(ProcessingResult::successful).map(ProcessingResult::message).toList();

        deleteSuccessfulMessages(successfulMessages);
    }

    private ProcessingResult processOneMessage(Message message) {

        String receiveCount = message.attributes().get(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT);

        try {
            log.info("Processing message. " + "messageId={}, receiveCount={}", message.messageId(), receiveCount);

            /*
             * Keep extending visibility while business
             * processing is running.
             */
            try (var lease = visibilityExtender.start(message)) {
                messageProcessor.process(message);
            }

            return ProcessingResult.success(message);

        } catch (Exception e) {
            log.error("SQS processing failed. " + "messageId={}, receiveCount={}", message.messageId(), receiveCount, e);
            /*
             * CRITICAL:
             *
             * Do not delete.
             *
             * Visibility timeout expires.
             * SQS delivers it again.
             *
             * Eventually redrive policy sends it to DLQ.
             */

            return ProcessingResult.failure(message);
        }
    }

    private void deleteSuccessfulMessages(List<Message> messages) {

        if (messages.isEmpty()) return;

        /*
         * DeleteMessageBatch maximum = 10.
         */

        for (int start = 0; start < messages.size(); start += 10) {
            int end = Math.min(start + 10, messages.size());
            deleteChunk(messages.subList(start, end));
        }
    }

    private void deleteChunk(List<Message> messages) {

        List<DeleteMessageBatchRequestEntry> entries = new ArrayList<>();

        for (int i = 0; i < messages.size(); i++) {

            Message message = messages.get(i);
            entries.add(DeleteMessageBatchRequestEntry.builder()
                            .id(Integer.toString(i))
                            .receiptHandle(message.receiptHandle())
                            .build()
            );
        }

        try {
            DeleteMessageBatchResponse response = sqsClient.deleteMessageBatch(DeleteMessageBatchRequest
                            .builder()
                            .queueUrl(properties.queueUrl())
                            .entries(entries)
                            .build());

            /*
             * IMPORTANT:
             *
             * AWS batch operations can return HTTP 200
             * and still contain individual failures.
             */

            if (!response.failed().isEmpty()) {
                response.failed().forEach(failure -> log.error("Failed deleting SQS " + "message. id={}, " + "code={}, " + "message={}", failure.id(), failure.code(), failure.message()));
            }

        } catch (Exception e) {
            /*
             * Do not panic.
             *
             * Successfully processed messages will
             * eventually reappear.
             *
             * Idempotency protection below will prevent
             * business logic from running twice.
             */

            log.error("DeleteMessageBatch failed", e);
        }
    }
}