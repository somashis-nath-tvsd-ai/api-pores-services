package com.igot.cb.execution.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.pores.cache.ICacheService;
import com.igot.cb.pores.dto.CustomResponse;
import com.igot.cb.pores.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.repository.Repository;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExecutionService {



    default CustomResponse createOrUpdate(JsonNode payload, String schema,
                                          ValidationService validationService,
                                          ReadSaveStartegyService readSaveStrategyService,
                                          DataTransformerService dataTransformerService, UtilityService utilityService,
                                          List<Utilities> utilityListAttach, Repository entityRepository,
                                          CacheStrategyService cacheStrategyService, ICacheService icacheService) throws JsonProcessingException
    {
        CustomResponse response = new CustomResponse();
        ObjectMapper objectMapper = new ObjectMapper();

        //validate - the schema or input data
        boolean validation = validationService.validate(schema,payload);
        //Get the schema & repository of the provided data
        ExecutionEntity savedEntityObject; // schema object
        Object entityObject;

        //selection to proceed further common for all the process
        if(!validation){
            response.setMessage(Constants.FAILED);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }else{
            //forking for save or update
            if(payload.get(Constants.ID) == null){
                //flow for save
                //utilityService.preSaveUtility(utilityListAttach,entityObject);
                savedEntityObject = (ExecutionEntity) dataTransformerService.beforeSaveTransformation(payload,"save",entityRepository);
                savedEntityObject = (ExecutionEntity) readSaveStrategyService.saveData(savedEntityObject,entityRepository); // support for schema less -- approach in multiple table or multiple services
                ExecutionEntity transformObject = (ExecutionEntity) dataTransformerService.afterSaveTransformation(savedEntityObject);
                if(null !=savedEntityObject){
                    response.setResponseCode(HttpStatus.CREATED);
                    response.setMessage("Data Created");
                    response
                            .getResult()
                            .put(Constants.RESULT, objectMapper.readValue(transformObject.getData().toString(), JsonNode.class));
                }
                cacheStrategyService.dataSaveCache(savedEntityObject.getId(),savedEntityObject,icacheService);
                //utilityService.afterSaveUtility(utilityListAttach,transformObject);
            }else{
                //flow for update
                //utilityService.preUpdateUtility(utilityListAttach,entityObject);
                entityObject = dataTransformerService.beforeSaveTransformation(payload,"update",entityRepository);
                savedEntityObject = (ExecutionEntity) readSaveStrategyService.saveData(entityObject,entityRepository);
                ExecutionEntity transformObject = (ExecutionEntity) dataTransformerService.afterSaveTransformation(savedEntityObject);
                if(null !=savedEntityObject){
                    response.setResponseCode(HttpStatus.ACCEPTED);
                    response.setMessage("Data Updated");
                    response
                            .getResult()
                            .put(Constants.RESULT, objectMapper.readValue(transformObject.getData().toString(), JsonNode.class));
                }
                cacheStrategyService.dataSaveCache(savedEntityObject.getId(),savedEntityObject,icacheService);
                //utilityService.afterUpdateUtility(utilityListAttach,savedEntityObject);
            }
        }
        return response;
    }

    default CustomResponse read(JsonNode payload, String contentSchema, ValidationService validationService,
                                ReadSaveStartegyService readSaveStrategyService, DataTransformerService dataTransformerService,
                                UtilityService utilityService, List<Utilities> utilityListAttach, Repository entityRepository,
                                CacheStrategyService cacheStrategyService, ICacheService icacheService) throws IOException
    {
        CustomResponse response = new CustomResponse();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,Object> contentParamMap = dataTransformerService.beforeReadDataTransformers(payload);
        //check if the param contains only ID
        //future scope to fork two different flows -- single / multiple param
        if(contentParamMap.containsKey("id")) {
            String utilityData = (String) cacheStrategyService.dataFromCache(contentParamMap.get("id").toString(),icacheService);
            if (StringUtils.isEmpty(utilityData)){
                //this line of code can be
                Optional<ExecutionEntity> entityData = (Optional<ExecutionEntity>) readSaveStrategyService.dataFromDBUsingPrimaryKey(String.valueOf(contentParamMap.get("id")),entityRepository);
                if(entityData.isPresent()){
                    response
                            .getResult()
                            .put(Constants.RESULT, objectMapper.readValue(entityData.get().getData().toString(), JsonNode.class));
                    response.setResponseCode(HttpStatus.OK);
                    //response.setMessage(entityData.get().getData().toString());
                }else{
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                }
            }


        }else{
            response.setMessage("Not supported on normal flow, Need custom implementation");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
        }

        return response;
    }

    //CustomResponse searchEntity(SearchCriteria searchCriteria);

    default CustomResponse delete(JsonNode payload, String schema,
                                  ValidationService validationService,
                                  ReadSaveStartegyService readSaveStrategyService,
                                  DataTransformerService dataTransformerService, UtilityService utilityService,
                                  List<Utilities> utilityListAttach, Repository entityRepository, CacheStrategyService cacheStrategyService, ICacheService icacheService){
        CustomResponse response = new CustomResponse();
        Map<String,Object> contentParamMap = dataTransformerService.beforeReadDataTransformers(payload);
        if(contentParamMap.containsKey("id")){
            readSaveStrategyService.deleteByPrimaryKey(String.valueOf(contentParamMap.get("id")),entityRepository);
            response.setResponseCode(HttpStatus.OK);
            response.setMessage("Deleted Successfully");
        }else{
//            readSaveStrategyService.deleteByMultipleParam(contentParamMap,entityRepository);
//            response.setResponseCode(HttpStatus.OK);
//            response.setMessage("Deleted Successfully");
            response.setMessage("Not supported on normal flow, Need custom implementation");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
        }

        return response;
    }

}
