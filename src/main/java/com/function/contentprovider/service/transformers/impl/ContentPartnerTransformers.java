package com.function.contentprovider.service.transformers.impl;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.function.execution.basic.ExecutionDefaultRepository;
import com.function.execution.component.core.ExecutionEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.function.execution.component.service.DataTransformerService;
import com.function.pores.util.Constants;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;


@Service
public class ContentPartnerTransformers implements DataTransformerService{

    @Autowired
    private ExecutionDefaultRepository entityRepository;

    @Override
    public ExecutionEntity beforeSaveTransformation(String schema, JsonNode payload, String operationType, Repository entityRepository) {
        ExecutionEntity jsonNodeEntity = new ExecutionEntity();
        if(operationType.equals("save")){
            String id = String.valueOf(UUID.randomUUID());
            ((ObjectNode) payload).put(Constants.IS_ACTIVE, Constants.ACTIVE_STATUS);
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            
            jsonNodeEntity.setId(id);
            jsonNodeEntity.setData(payload);
            jsonNodeEntity.setCreatedOn(currentTime);
            jsonNodeEntity.setUpdatedOn(currentTime);
            jsonNodeEntity.setSchema(schema);

            //return jsonNodeEntity;

        }else if(operationType.equals("update")){
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            String exitingId = payload.get("id").asText();
            Optional<ExecutionEntity> content = this.entityRepository.findById(exitingId);
            ObjectNode objectNode = (ObjectNode) payload;
            objectNode.remove("id");
            jsonNodeEntity = content.get();
            jsonNodeEntity.setData(objectNode);
            jsonNodeEntity.setUpdatedOn(currentTime);
        }

        return jsonNodeEntity;

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Object> afterSaveTransformation(ExecutionEntity saveJsonEntity) {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> dataMap = objectMapper.convertValue(saveJsonEntity.getData(), Map.class);
        Map<String, Object> map = objectMapper.convertValue(saveJsonEntity, Map.class);
        map.remove("data");
        map.putAll(dataMap);
        //throw new UnsupportedOperationException("Not supported yet.");

        return (Map<String, Object>) map;
    }


    @Override
    public Object afterReadDataTransformers(JsonNode payload) {
        return null;
    }

    @Override
    public Map<String,Object> beforeReadDataTransformers(JsonNode payload) {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.convertValue(payload, Map.class);

        return map;

        //throw new UnsupportedOperationException("Not supported yet.");
    }


    

    


}