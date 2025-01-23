package com.igot.cb.execution.basic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.execution.component.DataTransformerService;

import java.util.Map;

public class DefaultDataTransformerServiceImpl implements DataTransformerService {
    @Override
    public Object afterSaveTransformation(Object savedEntity) {
        return savedEntity;
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
