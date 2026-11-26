package com.analyticalrx.dataplatform.reportupload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class ReportUploadScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReportUploadScheduler.class);

    private final UploadReceiptRepository receipts;
    private final TransactionTemplate transaction;
    private final S3Client s3;
    private final String bucket;
    private final Map<String, ReportUploadWorkflow> workflowsByReportType = new HashMap<>();

    ReportUploadScheduler(UploadReceiptRepository receipts, TransactionTemplate transaction, S3Client s3,
                          String bucket, List<ReportUploadWorkflow> workflows) {
        this.receipts = receipts;
        this.transaction = transaction;
        this.s3 = s3;
        this.bucket = bucket;

        if (workflows.isEmpty()) {
            throw new IllegalStateException("Report upload processing requires at least one workflow");
        }

        for (ReportUploadWorkflow workflow : workflows) {
            String reportType = workflow.reportType();
            if (reportType == null || reportType.isBlank()) {
                throw new IllegalStateException("Report upload workflow must declare a report type");
            }
            if (workflowsByReportType.putIfAbsent(reportType, workflow) != null) {
                throw new IllegalStateException("Multiple report upload workflows registered for " + reportType);
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.report-uploads.poll-delay:5s}", scheduler = "reportUploadTaskScheduler")
    void poll() {
        try {
            Optional<Long> receiptToRetry = transaction.execute(this::processNextReceipt);
            if (receiptToRetry.isPresent()) {
                receipts.deferRetry(receiptToRetry.get());
            }
        } catch (RuntimeException failure) {
            log.warn("Could not poll report upload receipts ({})", failure.getClass().getSimpleName());
        }
    }

    private Optional<Long> processNextReceipt(TransactionStatus status) {
        Optional<UploadReceipt> next = receipts.lockNextSavedReceipt();
        if (next.isEmpty()) {
            return Optional.empty();
        }

        UploadReceipt receipt = next.get();
        ReportUploadWorkflow workflow = workflowsByReportType.get(receipt.reportType());
        if (workflow == null) {
            log.warn("No report workflow registered for upload {}", receipt.uploadId());
            return Optional.of(receipt.uploadId());
        }

        try {
            processReceipt(receipt, workflow);
            return Optional.empty();
        } catch (IOException | RuntimeException failure) {
            status.setRollbackOnly();
            log.warn("Report upload {} will retry ({})", receipt.uploadId(), failure.getClass().getSimpleName());
            return Optional.of(receipt.uploadId());
        }
    }

    private void processReceipt(UploadReceipt receipt, ReportUploadWorkflow workflow) throws IOException {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(receipt.objectKey())
                .build();

        log.debug("Processing report upload {}", receipt.uploadId());

        ReportUploadResult result;
        try (InputStream report = s3.getObject(request)) {
            result = workflow.process(receipt, report);
        }

        switch (result) {
            case ReportUploadResult.Processed processed ->
                    receipts.markProcessed(receipt.uploadId(), processed.rowCount());
            case ReportUploadResult.Rejected rejected ->
                    receipts.markFailed(receipt.uploadId(), rejected.errorCode());
        }
    }
}
