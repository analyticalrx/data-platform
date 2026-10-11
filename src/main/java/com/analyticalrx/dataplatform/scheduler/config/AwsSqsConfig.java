package com.analyticalrx.dataplatform.scheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.apache5.Apache5HttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.time.Duration;

@Configuration
public class AwsSqsConfig {

    @Bean
    public SqsClient sqsClient(@Value("${app.sqs.region}") String region) {
        return SqsClient.builder()
                .region(Region.of(region))
                .httpClientBuilder(
                        Apache5HttpClient.builder()
                                .maxConnections(20)
                                .connectionTimeout(Duration.ofSeconds(5))
                                .connectionAcquisitionTimeout(Duration.ofSeconds(10))
                                .socketTimeout(Duration.ofSeconds(30))
                )
                .overrideConfiguration(config ->
                        config.apiCallAttemptTimeout(Duration.ofSeconds(35))
                )
                .build();
    }
}
