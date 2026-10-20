package com.analyticalrx.dataplatform.scheduler.service.impl;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import com.analyticalrx.dataplatform.scheduler.dto.EventEnvelope;
import com.analyticalrx.dataplatform.scheduler.dto.Meta;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandler;
import com.analyticalrx.dataplatform.scheduler.handler.PrimeRxEventHandlerRegistry;
import com.analyticalrx.dataplatform.scheduler.service.IngestionPipelineProcessingService;
import com.analyticalrx.dataplatform.scheduler.service.SqsMessageProcessor;
import com.analyticalrx.dataplatform.scheduler.utils.JsonNodeProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.model.Message;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsMessageProcessorImpl implements SqsMessageProcessor {

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final IngestionPipelineProcessingService businessService;
    private final JsonNodeProcessor nodeProcessor;
    private final PrimeRxEventHandlerRegistry handlerRegistry;

    @Transactional
    public void process(Message message) throws Exception {

        JsonNode root = objectMapper.readTree(message.body());
        Meta meta = nodeProcessor.mapIfPresent(root, "meta", Meta.class);

        PrimeRxEventType eventType = PrimeRxEventType.fromValue(meta.getEventType());

        PrimeRxEventHandler handler = handlerRegistry.getHandler(eventType);

        handler.process(meta, root);

//        EventEnvelope event = objectMapper.readValue(message.body(), EventEnvelope.class);

        /*
         * PostgreSQL example.
         *
         * INSERT + business changes participate
         * in the SAME DB transaction.
         */

        int inserted =
                jdbcTemplate.update(
                        """
                                INSERT INTO sqs_message_inbox
                                    (event_id, processed_at)
                                VALUES
                                    (?, CURRENT_TIMESTAMP)
                                ON CONFLICT (event_id)
                                DO NOTHING
                                """,
                        "TEST"
//                        event.eventId()
                );

        if (inserted == 0) {

            /*
             * Another EC2 already successfully
             * processed this event.
             */

//            log.info("Duplicate event ignored. eventId={}", event.eventId());

            return;
        }

        /*
         * Business DB operations.
         */

//        businessService.process(event);

        /*
         * Transaction commits here:
         *
         * inbox entry
         * +
         * business DB changes
         *
         * commit atomically.
         */
    }
}