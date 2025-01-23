package com.igot.cb.contentprovider.service.execution.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.execution.component.*;
import com.igot.cb.pores.cache.CacheService;
import com.igot.cb.pores.cache.ICacheService;
import com.igot.cb.pores.dto.CustomResponse;
import com.igot.cb.pores.dto.RespParam;
import com.igot.cb.pores.elasticsearch.service.EsUtilService;
import com.igot.cb.pores.util.Constants;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ContentPartnerServiceImpl implements ExecutionService {

    @Autowired
    private EsUtilService esUtilService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ApplicationContext applicationContext;


    @Autowired
    private ObjectMapper objectMapper;
    private Logger logger = LoggerFactory.getLogger(ContentPartnerServiceImpl.class);



    /**
     *
     * Its a generalize approach to execute save and update operation that can be used to any resource Create or Update
     * operation.
     */

    @Override
    public CustomResponse createOrUpdate(JsonNode payload,
                                         String schema, ValidationService validationService,
                                         ReadSaveStartegyService readSaveStrategyService,
                                         DataTransformerService dataTransformerService,
                                         UtilityService utilityService, List<Utilities> utilityListAttach,
                                         Repository entityRepository, CacheStrategyService cacheStrategyService, ICacheService icacheService) throws JsonProcessingException {
        CustomResponse response = new CustomResponse();

        //validate - the schema or input data
        boolean validation = validationService.validate(schema,payload);
        //Get the schema & repository of the provided data
        ExecutionEntity savedEntityObject; // schema object

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
                Object transformObject = dataTransformerService.afterSaveTransformation(savedEntityObject);
                if(null !=savedEntityObject){
                    response.setResponseCode(HttpStatus.CREATED);
                    response.setMessage("Data Created");
                    response
                            .getResult()
                            .put(Constants.RESULT, objectMapper.readValue(transformObject.toString(), new TypeReference<ExecutionEntity>() {
                            }));
                }
                cacheStrategyService.dataSaveCache(savedEntityObject.getId(),savedEntityObject,icacheService);
                //utilityService.afterSaveUtility(utilityListAttach,transformObject);
            }else{
                //flow for update
                //utilityService.preUpdateUtility(utilityListAttach,entityObject);
                savedEntityObject = (ExecutionEntity) dataTransformerService.beforeSaveTransformation(payload,"update",entityRepository);
                savedEntityObject = (ExecutionEntity) readSaveStrategyService.saveData(savedEntityObject,entityRepository);
                Object transformObject = dataTransformerService.afterSaveTransformation(savedEntityObject);
                if(null !=savedEntityObject){
                    response.setResponseCode(HttpStatus.ACCEPTED);
                    response.setMessage("Data Updated");
                    response
                            .getResult()
                            .put(Constants.RESULT, objectMapper.readValue(transformObject.toString(), new TypeReference<ExecutionEntity>() {
                            }));
                }
                cacheStrategyService.dataSaveCache(savedEntityObject.getId(),savedEntityObject,icacheService);
                //utilityService.afterUpdateUtility(utilityListAttach,savedEntityObject);
            }
        }
        return response;

    }

    @Override
    public CustomResponse read(JsonNode contentPartnerDetails, String contentSchema, ValidationService validationService,
                               ReadSaveStartegyService readSaveStrategyService, DataTransformerService dataTransformerService,
                               UtilityService utilityService, List<Utilities> utilityListAttach, Repository entityRepository, CacheStrategyService cacheStrategyService, ICacheService icacheService) throws IOException {

        Object obtainedBean = applicationContext.getAutowireCapableBeanFactory().getBean(contentSchema);


        CustomResponse response = new CustomResponse();

        //Class<?> entityClass = entityObject.getClass();

        //check if only one element is there and too id only

        Map<String,Object> contentParamMap = dataTransformerService.beforeReadDataTransformers(contentPartnerDetails);
        //check if the param contains only ID
        //future scope to fork two different flows -- single / multiple param
        if(contentParamMap.containsKey("id")){
            String utilityData = (String) cacheStrategyService.dataFromCache(contentParamMap.get("id").toString(),icacheService);
            if (StringUtils.isEmpty(utilityData)){
                //this line of code can be
                Optional<ExecutionEntity> entityData = (Optional<ExecutionEntity>) readSaveStrategyService.dataFromDBUsingPrimaryKey(String.valueOf(contentParamMap.get("id")),entityRepository);
                if(entityData.isPresent()){
                    response
                            .getResult()
                            .put(Constants.RESULT, objectMapper.readValue(entityData.get().getData().toString(), new TypeReference<ExecutionEntity>() {
                            }));
                    response.setResponseCode(HttpStatus.OK);
                    //response.setMessage(entityData.get().getData().toString());
                }else{
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                }
            }

        }else{

            Optional<List<Object>> entityData = Optional.ofNullable((List<Object>) readSaveStrategyService.dataFromDbUsingQueryParam(contentParamMap, entityRepository));
            if(entityData.isPresent()){

                //objectMapper.writeValueAsString(entityData.get());

                response
                        .getResult()
                        .put(Constants.RESULT,
                                objectMapper.readValue(objectMapper.writeValueAsString(entityData.get()), new TypeReference<List<ExecutionEntity>>() {
                        }));

                response.setResponseCode(HttpStatus.OK);
                //response.setMessage(objectMapper.writeValueAsString(entityData.get()));
            }else{
                response.setResponseCode(HttpStatus.BAD_REQUEST);
            }
        }

        return response;
    }





    @Override
    public CustomResponse delete(JsonNode payload,
                                 String schema, ValidationService validationService,
                                 ReadSaveStartegyService readSaveStrategyService,
                                 DataTransformerService dataTransformerService,
                                 UtilityService utilityService, List<Utilities> utilityListAttach,
                                 Repository entityRepository, CacheStrategyService cacheStrategyService, ICacheService icacheService) {
        CustomResponse response = new CustomResponse();

        Optional<ExecutionEntity> optionEntity;
        Optional<List<Object>> optionEntityList;
        Map<String,Object> contentParamMap = dataTransformerService.beforeReadDataTransformers(payload);
        if(contentParamMap.containsKey("id")){
            readSaveStrategyService.deleteByPrimaryKey(String.valueOf(contentParamMap.get("id")),entityRepository);
            response.setResponseCode(HttpStatus.OK);
            response.setMessage("Deleted Successfully");
        }else{
            readSaveStrategyService.deleteByMultipleParam(contentParamMap,entityRepository);
            response.setResponseCode(HttpStatus.OK);
            response.setMessage("Deleted Successfully");
        }

        return response;
    }

    

    public void createSuccessResponse(CustomResponse response) {
        response.setParams(new RespParam());
        response.getParams().setStatus(Constants.SUCCESS);
        response.setResponseCode(HttpStatus.OK);
    }

    public void createErrorResponse(CustomResponse response, String errorMessage, HttpStatus httpStatus, String status) {
        response.setParams(new RespParam());
        response.getParams().setStatus(status);
        response.setResponseCode(httpStatus);
    }
}
