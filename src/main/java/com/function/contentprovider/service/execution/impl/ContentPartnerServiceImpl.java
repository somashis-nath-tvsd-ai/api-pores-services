package com.function.contentprovider.service.execution.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.function.execution.component.api.ExecutionService;
import com.function.execution.component.api.ParameterContext;
import com.function.execution.component.core.ExecutionContextBean;
import com.function.execution.component.core.ExecutionEntity;
import com.function.pores.cache.CacheService;
import com.function.pores.dto.CustomResponse;
import com.function.pores.dto.RespParam;
import com.function.pores.elasticsearch.dto.SearchCriteria;
import com.function.pores.elasticsearch.dto.SearchResult;
import com.function.pores.elasticsearch.service.EsUtilService;
import com.function.pores.util.Constants;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
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
    public CustomResponse createOrUpdate(JsonNode data, ExecutionContextBean executionContextBean) throws JsonProcessingException {
        CustomResponse response = new CustomResponse();
        String schema = executionContextBean.getParameterContext().getContentSchema(data);
        JsonNode payload = executionContextBean.getParameterContext().getContentData(data);
        //validate - the schema or input data
        boolean validation = executionContextBean.getValidationService().validate(schema,payload);
        //Get the schema & repository of the provided data
        ExecutionEntity savedEntityObject; // schema object

        //selection to proceed further common for all the process
        if(!validation){
            response.setMessage("Data Validation Failed");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }else{
            //forking for save or update
            if(payload.get(Constants.ID) == null){
                //flow for save
                //utilityService.preSaveUtility(utilityListAttach,entityObject);
                savedEntityObject = (ExecutionEntity) executionContextBean.getDataTransformerService().beforeSaveTransformation(schema,payload,"save",executionContextBean.getEntityRepository());
                savedEntityObject = (ExecutionEntity) executionContextBean.getReadSaveStrategyService().saveData(savedEntityObject,executionContextBean.getEntityRepository()); // support for schema less -- approach in multiple table or multiple services
                Map<String, Object> transformObject = (Map<String, Object>) executionContextBean.getDataTransformerService().afterSaveTransformation(savedEntityObject);
                if(null !=savedEntityObject){
                    response.setResponseCode(HttpStatus.CREATED);
                    response.setMessage("Data Created");
                    response
                            .getResult()
                            .put(Constants.RESULT, objectMapper.readValue(savedEntityObject.getData().toString(), JsonNode.class));
                }
                executionContextBean.getCacheStrategyService().dataSaveCache(savedEntityObject.getId(),savedEntityObject,executionContextBean.getIcacheService());
                executionContextBean.getEsUtilService().validateSearchSchema(schema.toLowerCase(Locale.ROOT));
                executionContextBean.getEsUtilService().addDocument(schema.toLowerCase(Locale.ROOT), Constants.INDEX_TYPE, savedEntityObject.getId(), transformObject);
                //utilityService.afterSaveUtility(utilityListAttach,transformObject);
            }else{
                //flow for update
                //utilityService.preUpdateUtility(utilityListAttach,entityObject);
                savedEntityObject = (ExecutionEntity) executionContextBean.getDataTransformerService().beforeSaveTransformation(schema, payload,"update",executionContextBean.getEntityRepository());
                savedEntityObject = (ExecutionEntity) executionContextBean.getReadSaveStrategyService().saveData(savedEntityObject,executionContextBean.getEntityRepository());
                Map<String, Object> transformObject = (Map<String, Object>) executionContextBean.getDataTransformerService().afterSaveTransformation(savedEntityObject);
                if(null !=savedEntityObject){
                    response.setResponseCode(HttpStatus.ACCEPTED);
                    response.setMessage("Data Updated");
                    response
                            .getResult()
                            .put(Constants.RESULT, objectMapper.readValue(savedEntityObject.getData().toString(), JsonNode.class));
                }
                executionContextBean.getCacheStrategyService().dataSaveCache(savedEntityObject.getId(),savedEntityObject,executionContextBean.getIcacheService());
                executionContextBean.getEsUtilService().validateSearchSchema(schema);
                executionContextBean.getEsUtilService().updateDocument(schema.toLowerCase(Locale.ROOT), Constants.INDEX_TYPE, savedEntityObject.getId(), (Map<String, Object>) transformObject);
                //utilityService.afterUpdateUtility(utilityListAttach,savedEntityObject);
            }
        }
        return response;

    }

    @Override
    public CustomResponse read(ParameterContext parameterContext,JsonNode data, ExecutionContextBean executionContextBean) throws IOException {

        String contentSchema = parameterContext.getContentSchema(data);
        JsonNode contentPartnerDetails = parameterContext.getContentData(data);

        Object obtainedBean = applicationContext.getAutowireCapableBeanFactory().getBean(contentSchema);


        CustomResponse response = new CustomResponse();

        //Class<?> entityClass = entityObject.getClass();

        //check if only one element is there and too id only

        Map<String,Object> contentParamMap = executionContextBean.getDataTransformerService().beforeReadDataTransformers(contentPartnerDetails);
        //check if the param contains only ID
        //future scope to fork two different flows -- single / multiple param
        if(contentParamMap.containsKey("id")){
            String utilityData = (String) executionContextBean.getCacheStrategyService().dataFromCache(contentParamMap.get("id").toString(),executionContextBean.getIcacheService());
            if (StringUtils.isEmpty(utilityData)){
                //this line of code can be
                Optional<ExecutionEntity> entityData = (Optional<ExecutionEntity>) executionContextBean.getReadSaveStrategyService().dataFromDBUsingPrimaryKey(String.valueOf(contentParamMap.get("id")),executionContextBean.getEntityRepository());
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

            Optional<List<Object>> entityData = Optional.ofNullable((List<Object>) executionContextBean.getReadSaveStrategyService().dataFromDbUsingQueryParam(contentParamMap, executionContextBean.getEntityRepository()));
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
    public CustomResponse delete(ParameterContext parameterContext, JsonNode data,
                                 EsUtilService esUtilService, ExecutionContextBean executionContextBean) {
        CustomResponse response = new CustomResponse();

        String schema = parameterContext.getContentSchema(data);
        JsonNode payload = parameterContext.getContentData(data);

        Optional<ExecutionEntity> optionEntity;
        Optional<List<Object>> optionEntityList;
        Map<String,Object> contentParamMap = executionContextBean.getDataTransformerService().beforeReadDataTransformers(payload);
        if(contentParamMap.containsKey("id")){
            executionContextBean.getReadSaveStrategyService().deleteByPrimaryKey(String.valueOf(contentParamMap.get("id")),executionContextBean.getEntityRepository());
            response.setResponseCode(HttpStatus.OK);
            response.setMessage("Deleted Successfully");
        }else{
            executionContextBean.getReadSaveStrategyService().deleteByMultipleParam(contentParamMap,executionContextBean.getEntityRepository());
            response.setResponseCode(HttpStatus.OK);
            response.setMessage("Deleted Successfully");
        }

        return response;
    }

    @Override
    public CustomResponse search(ParameterContext parameterContext, JsonNode data, ExecutionContextBean executionContextBean) throws Exception {
        //log.info("ContentPartnerServiceImpl::searchEntity:searching the content partner");
        ObjectMapper objectMapper = new ObjectMapper();
        SearchCriteria searchCriteria = objectMapper.readValue(data.get("data").toString(),SearchCriteria.class);
        SearchResult searchResult = (SearchResult) executionContextBean.getEsUtilService().searchDocuments(parameterContext.getContentSchema(data).toLowerCase(Locale.ROOT),
                searchCriteria);
        CustomResponse response = new CustomResponse();
        response.getResult().put(Constants.RESULT, searchResult);
        createSuccessResponse(response);
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
