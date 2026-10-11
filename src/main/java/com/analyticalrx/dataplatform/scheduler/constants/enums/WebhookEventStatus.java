package com.analyticalrx.dataplatform.scheduler.constants.enums;

public enum WebhookEventStatus {
    received,
    queued,
    processing,
    processed,
    failed,
    duplicate,
    unsupported_event_type,
    needs_enrichment
}