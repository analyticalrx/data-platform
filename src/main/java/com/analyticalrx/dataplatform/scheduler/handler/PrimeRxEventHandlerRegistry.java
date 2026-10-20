package com.analyticalrx.dataplatform.scheduler.handler;

import com.analyticalrx.dataplatform.scheduler.constants.enums.PrimeRxEventType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PrimeRxEventHandlerRegistry {

    private final Map<PrimeRxEventType, PrimeRxEventHandler> handlers;

    public PrimeRxEventHandlerRegistry(List<PrimeRxEventHandler> eventHandlers) {

        Map<PrimeRxEventType, PrimeRxEventHandler> registry = new EnumMap<>(PrimeRxEventType.class);

        for (PrimeRxEventHandler handler : eventHandlers) {

            PrimeRxEventHandler existing = registry.putIfAbsent(handler.getEventType(), handler);

            if (Objects.nonNull(existing)) {
                throw new IllegalStateException("Duplicate handler: " + handler.getEventType());
            }
        }

        this.handlers = Map.copyOf(registry);
    }

    public PrimeRxEventHandler getHandler(PrimeRxEventType eventType) {

        PrimeRxEventHandler handler = handlers.get(eventType);

        if (handler == null) {
            throw new IllegalArgumentException(
                    "No handler registered for: " + eventType
            );
        }

        return handler;
    }
}