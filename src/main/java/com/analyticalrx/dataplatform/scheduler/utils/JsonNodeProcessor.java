package com.analyticalrx.dataplatform.scheduler.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class JsonNodeProcessor {

    private final ObjectMapper objectMapper;

    @Autowired
    public JsonNodeProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T mapIfPresent(JsonNode parent, String fieldName, Class<T> targetClass) {

        if (Objects.isNull(parent) || parent.isNull()) return null;

        JsonNode node = parent.get(fieldName);

        if (Objects.isNull(node) || node.isNull()) return null;

        try {
            return objectMapper.treeToValue(node, targetClass);
        } catch (JsonProcessingException e) {
            String errorMessage = String.format("Failed to map node '%s' to %s :: %s", fieldName, targetClass.getSimpleName(), e);
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

}
