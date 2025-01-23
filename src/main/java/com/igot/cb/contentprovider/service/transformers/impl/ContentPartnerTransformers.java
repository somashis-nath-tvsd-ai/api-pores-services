package com.igot.cb.contentprovider.service.transformers.impl;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.igot.cb.execution.basic.ExecutionDefaultRepository;
import com.igot.cb.execution.component.ExecutionEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.igot.cb.execution.component.DataTransformerService;
import com.igot.cb.pores.util.Constants;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;


@Service
public class ContentPartnerTransformers implements DataTransformerService{

    @Autowired
    private ExecutionDefaultRepository entityRepository;

    @Override
    public ExecutionEntity beforeSaveTransformation(JsonNode payload, String operationType, Repository entityRepository) {
        ExecutionEntity jsonNodeEntity = new ExecutionEntity();
        if(operationType.equals("save")){
            String id = String.valueOf(UUID.randomUUID());
            ((ObjectNode) payload).put(Constants.IS_ACTIVE, Constants.ACTIVE_STATUS);
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            
            jsonNodeEntity.setId(id);
            jsonNodeEntity.setData(payload);
            jsonNodeEntity.setCreatedOn(currentTime);
            jsonNodeEntity.setUpdatedOn(currentTime);

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
    public Object afterSaveTransformation(Object saveJsonEntity) {

        ExecutionEntity contentEntity = (ExecutionEntity)saveJsonEntity;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode createdJson = objectMapper.convertValue(contentEntity, JsonNode.class);
        return createdJson;
        //throw new UnsupportedOperationException("Not supported yet.");
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