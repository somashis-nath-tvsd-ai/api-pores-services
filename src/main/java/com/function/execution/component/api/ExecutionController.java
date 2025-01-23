package com.function.execution.component.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.function.execution.basic.ExecutionDefaultRepository;
import com.function.execution.component.service.*;
import com.function.execution.basic.*;
import com.function.execution.component.core.ExecutionContextBean;
import com.function.execution.component.core.FactoryBeanEntity;
import com.function.execution.component.core.FactoryContextBean;
import com.function.execution.component.service.*;
import com.function.execution.component.util.Utilities;
import com.function.pores.cache.ICacheService;
import com.function.pores.dto.CustomResponse;
import com.function.pores.elasticsearch.service.EsUtilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class ExecutionController {


    @Autowired
    private FactoryValidationService factoryValidationService;

    @Autowired
    private ApplicationContext applicationContext;

    //context after schema validation
    private ExecutionService executionService;
    private ValidationService validationService;
    private ReadSaveStartegyService readSaveStrategyService;
    private DataTransformerService dataTransformerService;
    private UtilityService utilityService;
    private CacheStrategyService cacheStrategyService;
    private JpaRepository entityRepository;
    private ICacheService icacheService;
    List<Utilities> utilityListAttach = new ArrayList<>();

    //context before schema validation
    private ParameterContext parameterContext;
    private EsUtilService esUtilService;

    //context at configuration loading
    private Utilities utilities;

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    @Autowired
    private ExecutionDefaultRepository executionDefaultRepository;

    @Autowired
    private FactoryContextBean factoryContext;

    private ExecutionContextBean executionContextBean;

    @Operation(summary = "Load your configuration - Data Schema or Search Index")
    @PostMapping("/config")
    public ResponseEntity<CustomResponse> config(@Parameter(description = "Request Body includes Schema Name , ElasticSearch Index name for ES Index, Schema JSON ")@RequestBody JsonNode data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CustomResponse validResponse = factoryValidationService.configValidation(data);
        CustomResponse response = new CustomResponse();
        if(validResponse.getResponseCode().equals(HttpStatus.OK)) {
            String contentSchema = data.get("schema").textValue();
            boolean validation = factoryValidationService.validateSchema(contentSchema);
            if (!applicationContext.getAutowireCapableBeanFactory().containsBean(contentSchema+ "-bean")) {
                executionContextBean = new ExecutionContextBean(data,applicationContext);
                Map<String, Object> beansMap = new HashMap<>();
                beansMap.put(contentSchema, executionContextBean);
                FactoryBeanEntity beanHolder = new FactoryBeanEntity(beansMap);
                beanFactory.registerSingleton(contentSchema + "-bean", beanHolder);
            }else{
                Map<String, Object> beansFold = ((FactoryBeanEntity)applicationContext.getAutowireCapableBeanFactory().getBean(contentSchema + "-bean")).getBeansMap();
                executionContextBean = (ExecutionContextBean) beansFold.get(contentSchema);
            }

            response = executionContextBean.getExecutionService().loadConfiguration(data,applicationContext,validation,executionContextBean);


        }
        return new ResponseEntity<>(response, response.getResponseCode());
    }


    @PostMapping("/create")
    public ResponseEntity<CustomResponse> create(@RequestBody JsonNode data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CustomResponse validResponse = factoryValidationService.createUpdateValidation(data);
        CustomResponse response = new CustomResponse();
        if(validResponse.getResponseCode().equals(HttpStatus.OK)){
            String contentSchema = data.get("schema").textValue();
            boolean validation = factoryValidationService.validateSchema(contentSchema);
            if(validation) {
                //if (factoryContext.checkFactory(contentSchema)) {
                    if (!applicationContext.getAutowireCapableBeanFactory().containsBean(contentSchema+ "-bean")) {
                        executionContextBean = new ExecutionContextBean(data,applicationContext);
                        Map<String, Object> beansMap = new HashMap<>();
                        beansMap.put(contentSchema, executionContextBean);
                        FactoryBeanEntity beanHolder = new FactoryBeanEntity(beansMap);
                        beanFactory.registerSingleton(contentSchema + "-bean", beanHolder);
                    } else {

                        Map<String, Object> beansFold = ((FactoryBeanEntity)applicationContext.getAutowireCapableBeanFactory().getBean(contentSchema + "-bean")).getBeansMap();
                        executionContextBean = (ExecutionContextBean) beansFold.get(contentSchema);
                    }

                    //contextInitialization(contentSchema);
                    //Object entityObject = schemavalidationService.getSchema();
                    response = executionContextBean.getExecutionService().createOrUpdate( data, executionContextBean);
                    return new ResponseEntity<>(response, response.getResponseCode());
            }else{
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Schema Not Supported");
                return new ResponseEntity<>(response, response.getResponseCode());
            }

        }else{
            //response.setResponseCode(HttpStatus.BAD_REQUEST);
            //response.setMessage("Request Input is not proper");
            return new ResponseEntity<>(validResponse, validResponse.getResponseCode());
        }

        //return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody JsonNode data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CustomResponse validResponse = factoryValidationService.createUpdateValidation(data);
        CustomResponse response = new CustomResponse();
        if(validResponse.getResponseCode().equals(HttpStatus.OK)){
            String contentSchema = data.get("schema").textValue();
            boolean validation = factoryValidationService.validateSchema(contentSchema);
            if(validation){
                //if(factoryContext.checkFactory(contentSchema)) {
                    if (!applicationContext.getAutowireCapableBeanFactory().containsBean(contentSchema+ "-bean")) {
                        executionContextBean = new ExecutionContextBean(data,applicationContext);
                        Map<String, Object> beansMap = new HashMap<>();
                        beansMap.put(contentSchema, executionContextBean);
                        FactoryBeanEntity beanHolder = new FactoryBeanEntity(beansMap);
                        beanFactory.registerSingleton(contentSchema + "-bean", beanHolder);
                    } else {
                        Map<String, Object> beansFold = ((FactoryBeanEntity)applicationContext.getAutowireCapableBeanFactory().getBean(contentSchema + "-bean")).getBeansMap();
                        executionContextBean = (ExecutionContextBean) beansFold.get(contentSchema);
                    }
                    //Object entityObject = validationService.getSchema();
                    response = executionContextBean.getExecutionService().createOrUpdate(data, executionContextBean);
                    return new ResponseEntity<>(response, response.getResponseCode());
                //}
            }else{
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Schema Not Supported");
                return new ResponseEntity<>(response, response.getResponseCode());
            }
        }else{
            //response.setResponseCode(HttpStatus.BAD_REQUEST);
            //response.setMessage("Request Input is not proper");
            return new ResponseEntity<>(validResponse, validResponse.getResponseCode());
        }
        //return new ResponseEntity<>(response, response.getResponseCode());
    }

    //get data based on different type of param

    //sending the json data as request param - but also escaping special characters

    @PostMapping("/read")
    public ResponseEntity<?> read(@RequestBody JsonNode data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CustomResponse validResponse = factoryValidationService.readValidation(data);
        CustomResponse response = new CustomResponse();
        if(validResponse.getResponseCode().equals(HttpStatus.OK)){
            String contentSchema = data.get("schema").textValue();
            boolean validation = factoryValidationService.validateSchema(contentSchema);
            if(validation) {
                //if (factoryContext.checkFactory(contentSchema)) {
                    if (!applicationContext.getAutowireCapableBeanFactory().containsBean(contentSchema+ "-bean")) {
                        executionContextBean = new ExecutionContextBean(data,applicationContext);
                        Map<String, Object> beansMap = new HashMap<>();
                        beansMap.put(contentSchema, executionContextBean);
                        FactoryBeanEntity beanHolder = new FactoryBeanEntity(beansMap);
                        beanFactory.registerSingleton(contentSchema + "-bean", beanHolder);
                    } else {
                        Map<String, Object> beansFold = ((FactoryBeanEntity)applicationContext.getAutowireCapableBeanFactory().getBean(contentSchema + "-bean")).getBeansMap();
                        executionContextBean = (ExecutionContextBean) beansFold.get(contentSchema);
                    }
                    //Object entityObject = validationService.getSchema();
                    response = executionContextBean.getExecutionService().read(executionContextBean.getParameterContext(), data, executionContextBean);
                    return new ResponseEntity<>(response, response.getResponseCode());
                //}
            }else{
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Schema Not Supported");
                return new ResponseEntity<>(response, response.getResponseCode());
            }

        }else{
            //response.setResponseCode(HttpStatus.BAD_REQUEST);
            //response.setMessage("Request Input is not proper");
            return new ResponseEntity<>(validResponse, validResponse.getResponseCode());
        }
        //return new ResponseEntity<>(response, response.getResponseCode());
    }

    //delete bulk entry based out some param

    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody JsonNode data) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CustomResponse validResponse = factoryValidationService.deleteValidation(data);
        CustomResponse response = new CustomResponse();
        if(validResponse.getResponseCode().equals(HttpStatus.OK)){
            String contentSchema = data.get("schema").textValue();
            boolean validation = factoryValidationService.validateSchema(contentSchema);
            if(validation) {
                //if (factoryContext.checkFactory(contentSchema)) {
                    if (!applicationContext.getAutowireCapableBeanFactory().containsBean(contentSchema+ "-bean")) {
                        executionContextBean = new ExecutionContextBean(data,applicationContext);
                        Map<String, Object> beansMap = new HashMap<>();
                        beansMap.put(contentSchema, executionContextBean);
                        FactoryBeanEntity beanHolder = new FactoryBeanEntity(beansMap);
                        beanFactory.registerSingleton(contentSchema + "-bean", beanHolder);
                    } else {
                        Map<String, Object> beansFold = ((FactoryBeanEntity)applicationContext.getAutowireCapableBeanFactory().getBean(contentSchema + "-bean")).getBeansMap();
                        executionContextBean = (ExecutionContextBean) beansFold.get(contentSchema);
                    }
                    //Object entityObject = validationService.getSchema();
                    response = executionContextBean.getExecutionService().delete(executionContextBean.getParameterContext(), data,executionContextBean.getEsUtilService(), executionContextBean);
                    return new ResponseEntity<>(response, HttpStatus.OK);
               // }
            }else{
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Schema Not Supported");
                return new ResponseEntity<>(response, response.getResponseCode());
            }

        }else{
            //response.setResponseCode(HttpStatus.BAD_REQUEST);
            //response.setMessage("Request Input is not proper");
            return new ResponseEntity<>(validResponse, validResponse.getResponseCode());
        }
        //return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/search")
    public ResponseEntity<CustomResponse> search(@RequestBody JsonNode data) throws Exception {
        CustomResponse validResponse = factoryValidationService.searchValidation(data);
        CustomResponse response = new CustomResponse();
        if(validResponse.getResponseCode().equals(HttpStatus.OK)){
            String contentSchema = data.get("schema").textValue();

            boolean validation = factoryValidationService.validateSchema(contentSchema);
            if(validation) {
                //if (factoryContext.checkFactory(contentSchema)) {
                    if (!applicationContext.getAutowireCapableBeanFactory().containsBean(contentSchema+ "-bean")) {
                        executionContextBean = new ExecutionContextBean(data,applicationContext);
                        Map<String, Object> beansMap = new HashMap<>();
                        beansMap.put(contentSchema, executionContextBean);
                        FactoryBeanEntity beanHolder = new FactoryBeanEntity(beansMap);
                        beanFactory.registerSingleton(contentSchema + "-bean", beanHolder);
                    } else {
                        Map<String, Object> beansFold = ((FactoryBeanEntity)applicationContext.getAutowireCapableBeanFactory().getBean(contentSchema + "-bean")).getBeansMap();
                        executionContextBean = (ExecutionContextBean) beansFold.get(contentSchema);
                    }
                    //Object entityObject = schemavalidationService.getSchema();
                    response = executionContextBean.getExecutionService().search(executionContextBean.getParameterContext(),data,executionContextBean);
                    return new ResponseEntity<>(response, response.getResponseCode());
            }else{
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Schema Not Supported");
                return new ResponseEntity<>(response, response.getResponseCode());
            }

        }else{
            //response.setResponseCode(HttpStatus.BAD_REQUEST);
            //response.setMessage("Request Input is not proper");
            return new ResponseEntity<>(validResponse, validResponse.getResponseCode());
        }
        //return new ResponseEntity<>(response, response.getResponseCode());
    }

}
