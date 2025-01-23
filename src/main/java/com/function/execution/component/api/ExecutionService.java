package com.function.execution.component.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.function.execution.component.core.ExecutionContextBean;
import com.function.execution.component.core.ExecutionEntity;
import com.function.pores.dto.CustomResponse;
import com.function.pores.dto.RespParam;
import com.function.pores.elasticsearch.dto.SearchCriteria;
import com.function.pores.elasticsearch.dto.SearchResult;
import com.function.pores.elasticsearch.service.EsUtilService;
import com.function.pores.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public interface ExecutionService {



    default CustomResponse createOrUpdate(JsonNode data, ExecutionContextBean executionContextBean) throws JsonProcessingException
    {
        CustomResponse response = new CustomResponse();
        ObjectMapper objectMapper = new ObjectMapper();

        String schema = executionContextBean.getParameterContext().getContentSchema(data);
        JsonNode payload = executionContextBean.getParameterContext().getContentData(data);

        //validate - the schema or input data
        boolean validation = executionContextBean.getValidationService().validate(schema,payload);
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
                executionContextBean.getEsUtilService().addDocument(schema.toLowerCase(Locale.ROOT), Constants.INDEX_TYPE, savedEntityObject.getId(), (Map<String, Object>) transformObject);
                //utilityService.afterSaveUtility(utilityListAttach,transformObject);
            }else{
                //flow for update
                //utilityService.preUpdateUtility(utilityListAttach,entityObject);
                entityObject = executionContextBean.getDataTransformerService().beforeSaveTransformation(schema, payload,"update",executionContextBean.getEntityRepository());
                savedEntityObject = (ExecutionEntity) executionContextBean.getReadSaveStrategyService().saveData(entityObject,executionContextBean.getEntityRepository());
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

    default CustomResponse read(ParameterContext parameterContext, JsonNode data, ExecutionContextBean executionContextBean) throws IOException
    {
        CustomResponse response = new CustomResponse();
        ObjectMapper objectMapper = new ObjectMapper();
        String schema = parameterContext.getContentSchema(data);
        JsonNode payload = parameterContext.getContentData(data);
        Map<String,Object> contentParamMap = executionContextBean.getDataTransformerService().beforeReadDataTransformers(payload);
        //check if the param contains only ID
        //future scope to fork two different flows -- single / multiple param
        if(contentParamMap.containsKey("id")) {
            String utilityData = (String) executionContextBean.getCacheStrategyService().dataFromCache(contentParamMap.get("id").toString(),executionContextBean.getIcacheService());
            if (StringUtils.isEmpty(utilityData)){
                //this line of code can be
                Optional<ExecutionEntity> entityData = (Optional<ExecutionEntity>) executionContextBean.getReadSaveStrategyService().dataFromDBUsingPrimaryKey(String.valueOf(contentParamMap.get("id")),executionContextBean.getEntityRepository());
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

    default CustomResponse delete(ParameterContext parameterContext, JsonNode data,
                                  EsUtilService esUtilService, ExecutionContextBean executionContextBean){
        CustomResponse response = new CustomResponse();
        String schema = executionContextBean.getParameterContext().getContentSchema(data);
        JsonNode payload = executionContextBean.getParameterContext().getContentData(data);
        Map<String,Object> contentParamMap = executionContextBean.getDataTransformerService().beforeReadDataTransformers(payload);
        if(contentParamMap.containsKey("id")){
            executionContextBean.getReadSaveStrategyService().deleteByPrimaryKey(String.valueOf(contentParamMap.get("id")),executionContextBean.getEntityRepository());
            response.setResponseCode(HttpStatus.OK);
            response.setMessage("Deleted Successfully");
        }else{
//            readSaveStrategyService.deleteByMultipleParam(contentParamMap,entityRepository);
//            response.setResponseCode(HttpStatus.OK);
//            response.setMessage("Deleted Successfully");
            response.setMessage("Not supported on normal flow, Need custom implementation");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
        }
        executionContextBean.getEsUtilService().deleteDocument(String.valueOf(contentParamMap.get("id")),schema.toLowerCase(Locale.ROOT));

        return response;
    }

    default CustomResponse search(ParameterContext parameterContext, JsonNode data, ExecutionContextBean executionContextBean) throws Exception {
        //log.info("ContentPartnerServiceImpl::searchEntity:searching the content partner");
        ObjectMapper objectMapper = new ObjectMapper();
        SearchCriteria searchCriteria = objectMapper.readValue(data.get("data").toString(),SearchCriteria.class);
        SearchResult searchResult = (SearchResult) executionContextBean.getEsUtilService().searchDocuments(parameterContext.getContentSchema(data).toLowerCase(Locale.ROOT),
                searchCriteria);
        CustomResponse response = new CustomResponse();
        response.getResult().put(Constants.RESULT, searchResult);
        createSuccessResponse(response);
        return response;

//        ESCriteria esCriteria = (ESCriteria) executionContextBean.getEsUtilService().getSearchCriteria();
//        ESResult esResult = (ESResult) executionContextBean.getEsUtilService().getSearchResult();
//        String searchString = esCriteria.getSearchString();
//        CustomResponse response = new CustomResponse();
//        if (searchString != null && searchString.length() < 2) {
//            createErrorResponse(
//                    response,
//                    "Minimum 3 characters are required to search",
//                    HttpStatus.BAD_REQUEST,
//                    Constants.FAILED_CONST);
//            return response;
//        }
//        try {
//            esResult =
//                    executionContextBean.getEsUtilService().searchDocuments(Constants.INDEX_NAME, esCriteria);
//            response.getResult().put(Constants.RESULT, esResult);
//            createSuccessResponse(response);
//            return response;
//        } catch (Exception e) {
//            //logger.error("Error while processing to search", e);
//            createErrorResponse(response, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, Constants.FAILED_CONST);
//            return response;
//        }
    }


    default void createSuccessResponse(CustomResponse response) {
        response.setParams(new RespParam());
        response.getParams().setStatus(Constants.SUCCESS);
        response.setResponseCode(HttpStatus.OK);
    }

    default void createErrorResponse(CustomResponse response, String errorMessage, HttpStatus httpStatus, String status) {
        response.setParams(new RespParam());
        response.getParams().setStatus(status);
        response.setResponseCode(httpStatus);
    }


    default CustomResponse loadConfiguration(
             JsonNode data, ApplicationContext applicationContext, Boolean validation,ExecutionContextBean executionContextBean) throws IOException {

        ClassPathResource resource = new ClassPathResource("application.properties"); // A known file in resources
        Path resourcePath = Paths.get(resource.getFile().getParentFile().getAbsolutePath());
        CustomResponse response = new CustomResponse();
        if(null != data.get("search")){
            if(validation){
                Path jsonFilePath = Paths.get(resourcePath.toString() + "/EsFieldsmapping/", data.get("search").textValue() + ".json");
                if (Files.exists(jsonFilePath)) {
                    response.setMessage("File already present, Please rename the file");
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                } else {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String mappingContent = objectMapper.writeValueAsString(data.get("data"));
                    //String jsonMapping = Files.readString(jsonFilePath);

                    // Create an index request
                    Settings settings = Settings.builder()
                            .put("index.number_of_shards", 3)
                            .put("index.number_of_replicas", 2)
                            .build();

                    CreateIndexRequest request = new CreateIndexRequest(data.get("search").textValue())
                            .settings(settings)
                            .mapping(mappingContent, XContentType.JSON);
                    //request.source(mappingsJson, XContentType.JSON); // Set the mappings JSON

                    RestHighLevelClient client = (RestHighLevelClient) executionContextBean.getEsUtilService().getElasticSearchClient();

                    CreateIndexResponse createIndexResponse = client.indices()
                            .create(request, RequestOptions.DEFAULT);
                    // Create the index
                    //client.indices().create(request, RequestOptions.DEFAULT);
                    if(createIndexResponse.isAcknowledged()){
                        Files.write(jsonFilePath, mappingContent.getBytes(StandardCharsets.UTF_8));
                        response.setMessage("ElasticSearch Index file created");
                        response.setResponseCode(HttpStatus.CREATED);
                    }

                }
            }else{
                response.setMessage("Data Schema is not present, Please upload that first");
                response.setResponseCode(HttpStatus.BAD_REQUEST);
            }

        }else{
            Path jsonFilePath = Paths.get(resourcePath.toString() + "/payloadValidation/", data.get("schema").textValue() + ".json");
//            if (Files.exists(jsonFilePath)) {
//                response.setMessage("File already present, Please rename the file");
//                response.setResponseCode(HttpStatus.BAD_REQUEST);
//            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                String mappingContent = objectMapper.writeValueAsString(data.get("data"));
                Files.write(jsonFilePath, mappingContent.getBytes(StandardCharsets.UTF_8));
                //Files.write(jsonFilePath, data.get("data").binaryValue());
                response.setMessage("Files Saved");
                response.setResponseCode(HttpStatus.CREATED);
            //}
        }

        return response;
    }



}
