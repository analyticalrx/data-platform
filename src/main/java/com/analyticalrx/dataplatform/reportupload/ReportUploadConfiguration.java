package com.analyticalrx.dataplatform.reportupload;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionTemplate;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "app.report-uploads", name = "enabled", havingValue = "true")
@EnableScheduling
class ReportUploadConfiguration {

    @Bean(defaultCandidate = false)
    S3Client reportUploadS3Client() {
        return S3Client.create();
    }

    @Bean(defaultCandidate = false)
    ThreadPoolTaskScheduler reportUploadTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("report-upload-");
        return scheduler;
    }

    @Bean
    ReportUploadScheduler reportUploadScheduler(JdbcTemplate jdbc, TransactionTemplate transaction,
                                                @Qualifier("reportUploadS3Client") S3Client s3,
                                                @Value("${app.report-uploads.bucket}") String bucket,
                                                List<ReportUploadWorkflow> workflows) {
        if (bucket.isBlank()) {
            throw new IllegalArgumentException("Configure the S3 bucket for report uploads");
        }

        UploadReceiptRepository receipts = new UploadReceiptRepository(jdbc);
        return new ReportUploadScheduler(receipts, transaction, s3, bucket, workflows);
    }
}
