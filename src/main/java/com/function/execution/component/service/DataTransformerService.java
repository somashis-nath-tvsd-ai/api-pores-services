package com.function.execution.component.service;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.function.execution.component.core.ExecutionEntity;
import com.function.pores.util.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public interface DataTransformerService{

    default Object beforeSaveTransformation(String schema ,JsonNode payload, String operationType,
                                             Repository repository){
        ExecutionEntity jsonNodeEntity = new ExecutionEntity();
        JpaRepository entityRepository = (JpaRepository) repository;
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
            Optional<ExecutionEntity> content = entityRepository.findById(exitingId);
            ObjectNode objectNode = (ObjectNode) payload;
            objectNode.remove("id");
            jsonNodeEntity = content.get();
            jsonNodeEntity.setData(objectNode);
            jsonNodeEntity.setUpdatedOn(currentTime);
        }

        return jsonNodeEntity;
    }

    public Object afterSaveTransformation(ExecutionEntity savedEntity);

    public Map<String,Object> beforeReadDataTransformers(JsonNode payload);

    public Object afterReadDataTransformers(JsonNode payload);

}