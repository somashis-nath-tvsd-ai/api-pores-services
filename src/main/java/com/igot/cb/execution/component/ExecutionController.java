package com.igot.cb.execution.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.igot.cb.execution.basic.*;
import com.igot.cb.pores.cache.CacheService;
import com.igot.cb.pores.cache.ICacheService;
import com.igot.cb.pores.dto.CustomResponse;
import com.igot.cb.pores.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/execution")
public class ExecutionController {


    @Autowired
    private FactoryValidationService factoryValidationService;

    @Autowired
    private ApplicationContext applicationContext;
    private ExecutionService executionService;
    private ValidationService validationService;
    private ReadSaveStartegyService readSaveStrategyService;
    private DataTransformerService dataTransformerService;
    private UtilityService utilityService;
    private CacheStrategyService cacheStrategyService;
    private JpaRepository entityRepository;
    private ICacheService icacheService;
    List<Utilities> utilityListAttach = new ArrayList<>();

    @Autowired
    private ExecutionDefaultRepository executionDefaultRepository;

    @Autowired
    private FactoryContextBean factoryContext;

 
    @PostMapping("/create")
    public ResponseEntity<CustomResponse> create(@RequestBody JsonNode data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CustomResponse validResponse = factoryValidationService.createUpdateValidation(data);
        CustomResponse response = new CustomResponse();
        if(validResponse.getResponseCode().equals(HttpStatus.OK)){
            String contentSchema = data.get("schema").textValue();
            JsonNode contentData = data.get("data");
            boolean validation = factoryValidationService.validateSchema(contentSchema,contentData);
            if(validation) {
                if (factoryContext.checkFactory(contentSchema)) {
                    if (!applicationContext.getAutowireCapableBeanFactory().containsBean(contentSchema)) {
                        factoryContext.loadBeans(contentSchema);
                    }
                    contextInitialization(contentSchema);
                    //Object entityObject = schemavalidationService.getSchema();
                    response = executionService.createOrUpdate(contentData, contentSchema,
                            validationService, readSaveStrategyService, dataTransformerService,
                            utilityService, utilityListAttach, entityRepository, cacheStrategyService, icacheService);
                    return new ResponseEntity<>(response, response.getResponseCode());
                } else {
                    defaultInitialization();
                    response = executionService.createOrUpdate(contentData, contentSchema,
                            validationService, readSaveStrategyService, dataTransformerService,
                            utilityService, utilityListAttach, executionDefaultRepository, cacheStrategyService, icacheService);

                    return new ResponseEntity<>(response, response.getResponseCode());
                }
            }else{
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Schema Not Supported");
                return new ResponseEntity<>(response, response.getResponseCode());
            }

        }else{
            return new ResponseEntity<>(validResponse, validResponse.getResponseCode());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody JsonNode data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CustomResponse validResponse = factoryValidationService.createUpdateValidation(data);
        CustomResponse response = new CustomResponse();
        if(validResponse.getResponseCode().equals(HttpStatus.OK)){
            String contentSchema = data.get("schema").textValue();
            JsonNode contentData = data.get("data");
            boolean validation = factoryValidationService.validateSchema(contentSchema,contentData);
            if(validation){
                if(factoryContext.checkFactory(contentSchema)) {
                    if (!applicationContext.getAutowireCapableBeanFactory().containsBean(contentSchema)) {
                        factoryContext.loadBeans(contentSchema);
                    }
                    contextInitialization(contentSchema);
                    //Object entityObject = validationService.getSchema();
                    response = executionService.createOrUpdate(contentData, contentSchema,
                            validationService, readSaveStrategyService, dataTransformerService,
                            utilityService, utilityListAttach, entityRepository, cacheStrategyService, icacheService);
                    return new ResponseEntity<>(response, response.getResponseCode());
                }else{
                    defaultInitialization();
                    response = executionService.createOrUpdate(contentData,contentSchema,
                            validationService,readSaveStrategyService,dataTransformerService,
                            utilityService,utilityListAttach,executionDefaultRepository, cacheStrategyService, icacheService);

                    return new ResponseEntity<>(response, response.getResponseCode());
                }
            }else{
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Schema Not Supported");
                return new ResponseEntity<>(response, response.getResponseCode());
            }
        }else{
            return new ResponseEntity<>(validResponse, validResponse.getResponseCode());
        }
    }

    //get data based on different type of param

    //sending the json data as request param - but also escaping special characters

    @PostMapping("/read")
    public ResponseEntity<?> read(@RequestBody JsonNode data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CustomResponse validResponse = factoryValidationService.readValidation(data);
        CustomResponse response = new CustomResponse();
        if(validResponse.getResponseCode().equals(HttpStatus.OK)){
            String contentSchema = data.get("schema").textValue();
            JsonNode contentData = data.get("data");
            boolean validation = factoryValidationService.validateSchema(contentSchema,contentData);
            if(validation) {
                if (factoryContext.checkFactory(contentSchema)) {
                    if (!applicationContext.getAutowireCapableBeanFactory().containsBean(contentSchema)) {
                        factoryContext.loadBeans(contentSchema);
                    }
                    contextInitialization(contentSchema);
                    //Object entityObject = validationService.getSchema();
                    response = executionService.read(contentData, contentSchema, validationService,
                            readSaveStrategyService, dataTransformerService, utilityService,
                            utilityListAttach, entityRepository, cacheStrategyService, icacheService);
                    return new ResponseEntity<>(response, response.getResponseCode());
                } else {
                    defaultInitialization();
                    response = executionService.read(contentData, contentSchema,
                            validationService, readSaveStrategyService, dataTransformerService,
                            utilityService, utilityListAttach, executionDefaultRepository, cacheStrategyService, icacheService);

                    return new ResponseEntity<>(response, response.getResponseCode());
                }
            }else{
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Schema Not Supported");
                return new ResponseEntity<>(response, response.getResponseCode());
            }

        }else{
            return new ResponseEntity<>(validResponse, validResponse.getResponseCode());
        }
    }

    //delete bulk entry based out some param

    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody JsonNode data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CustomResponse validResponse = factoryValidationService.deleteValidation(data);
        CustomResponse response = new CustomResponse();
        if(validResponse.getResponseCode().equals(HttpStatus.OK)){
            String contentSchema = data.get("schema").textValue();
            JsonNode contentData = data.get("data");
            boolean validation = factoryValidationService.validateSchema(contentSchema,contentData);
            if(validation) {
                if (factoryContext.checkFactory(contentSchema)) {
                    if (!applicationContext.getAutowireCapableBeanFactory().containsBean(contentSchema)) {
                        factoryContext.loadBeans(contentSchema);
                    }
                    contextInitialization(contentSchema);
                    //Object entityObject = validationService.getSchema();
                    response = executionService.delete(contentData, contentSchema, validationService, readSaveStrategyService,
                            dataTransformerService, utilityService, utilityListAttach, entityRepository, cacheStrategyService, icacheService);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    defaultInitialization();
                    response = executionService.delete(contentData, contentSchema,
                            validationService, readSaveStrategyService, dataTransformerService,
                            utilityService, utilityListAttach, executionDefaultRepository,
                            cacheStrategyService, icacheService);

                    return new ResponseEntity<>(response, response.getResponseCode());
                }
            }else{
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Schema Not Supported");
                return new ResponseEntity<>(response, response.getResponseCode());
            }

        }else{
            return new ResponseEntity<>(validResponse, validResponse.getResponseCode());
        }
    }

    private void contextInitialization(String contentSchema){


        Object obtainedBean = applicationContext.getAutowireCapableBeanFactory().getBean(contentSchema);
        Map<String,Object> beanMap =  ((FactoryBeanEntity) obtainedBean).getBeansMap();
        for (Map.Entry<String, Object> entry : beanMap.entrySet())
        {
               if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.VALIDATOR_APPEND)){
                   if(null != entry.getValue()){
                       validationService = (ValidationService) entry.getValue();
                   }else{
                       validationService = new ValidationServiceDefaultImpl();
                   }
                   entityRepository = (JpaRepository) validationService.getRepository();
               }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.EXECUTOR_APPEND)){
                   if(null != entry.getValue()){
                       executionService = (ExecutionService) entry.getValue();
                   }else{
                       executionService = new ExecutionServiceDefaultImpl();
                   }
               }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.READSAVE_APPEND)){
                   if(null != entry.getValue()){
                       readSaveStrategyService = (ReadSaveStartegyService)entry.getValue();
                   }else{
                       readSaveStrategyService = new DefaultReadSaveStrategyImpl();
                   }
               }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.TRANSFORMER_APPEND)){
                   if(null != entry.getValue()){
                       dataTransformerService = (DataTransformerService)entry.getValue();
                   }else{
                       dataTransformerService = new DefaultDataTransformerServiceImpl();
                   }
               }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.CACHE_APPEND)){
                   if(null != entry.getValue()){
                       icacheService = (ICacheService) entry.getValue();
                   }else{
                       icacheService = new CacheService();
                   }
               }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.CACHE_STRATEGY)){
                   if(null != entry.getValue()){
                       cacheStrategyService = (CacheStrategyService) entry.getValue();
                   }else{
                       cacheStrategyService = new DefaultCacheServiceImpl();
                   }
               }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.UTILITY_APPEND)){
                   if(null != entry.getValue()){
                       utilityService = (UtilityService) entry.getValue();
                   }else{
                       utilityService = new DefaultUtilityServiceImpl();
                   }
               }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.UTILITIES_ATTACHMENT)){
                   if(null != entry.getValue()){
                       utilityListAttach.addAll((Collection<? extends Utilities>) entry.getValue());
                   }
               }

        }
    }

    private void defaultInitialization(){
        executionService = new ExecutionServiceDefaultImpl();
        validationService = new ValidationServiceDefaultImpl();
        readSaveStrategyService = new DefaultReadSaveStrategyImpl();
        dataTransformerService = new DefaultDataTransformerServiceImpl();
        utilityService = new DefaultUtilityServiceImpl();
        cacheStrategyService = new DefaultCacheServiceImpl();
        icacheService = new CacheService();
        utilityListAttach = new ArrayList<>();
    }
}
