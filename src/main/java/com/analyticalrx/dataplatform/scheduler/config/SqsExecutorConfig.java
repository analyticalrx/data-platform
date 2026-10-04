package com.analyticalrx.dataplatform.scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class SqsExecutorConfig {

    @Bean(name = "sqsProcessingExecutor")
    public ThreadPoolTaskExecutor sqsProcessingExecutor(SqsBatchProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("sqs-worker-");
        executor.setCorePoolSize(properties.processingConcurrency());
        executor.setMaxPoolSize(properties.processingConcurrency());
        executor.setQueueCapacity(properties.receiveBatchSize());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        return executor;
    }
}