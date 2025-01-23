package com.function.execution.basic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.function.execution.component.core.ExecutionEntity;
import com.function.execution.component.service.DataTransformerService;

import java.util.Map;

public class DefaultDataTransformerServiceImpl implements DataTransformerService {
    @Override
    public Map<String, Object> afterSaveTransformation(ExecutionEntity savedEntity) {


        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> dataMap = objectMapper.convertValue(savedEntity.getData(), Map.class);
        Map<String, Object> map = objectMapper.convertValue(savedEntity, Map.class);
        map.remove("data");
        map.putAll(dataMap);

        return (Map<String, Object>) map;
    }

    @Override
    public Map<String, Object> beforeReadDataTransformers(JsonNode payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.convertValue(payload, Map.class);

        return map;
    }

    @Override
    public Object afterReadDataTransformers(JsonNode payload) {
        return payload;
    }
}
