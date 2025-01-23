package com.function.execution.component.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.function.execution.basic.*;
import com.function.execution.component.service.*;
import com.function.execution.component.api.ExecutionService;
import com.function.execution.component.api.ParameterContext;
import com.function.execution.component.util.Utilities;
import com.function.pores.cache.CacheService;
import com.function.pores.cache.ICacheService;
import com.function.pores.elasticsearch.config.EsConfig;
import com.function.pores.elasticsearch.service.EsUtilService;
import com.function.pores.elasticsearch.service.EsUtilServiceImpl;
import com.function.pores.util.Constants;
import lombok.Getter;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Getter
public class ExecutionContextBean {


    private FactoryContextBean factoryContext = new FactoryContextBean();

    @Autowired
    private FactoryValidationService factoryValidationService;

    @Autowired
    private ApplicationContext applicationContext;

    private String id;
    private String beanName;

    private ExecutionService executionService;
    private ValidationService validationService;
    private ReadSaveStartegyService readSaveStrategyService;
    private DataTransformerService dataTransformerService;
    private UtilityService utilityService;
    private CacheStrategyService cacheStrategyService;
    private JpaRepository entityRepository;
    private ICacheService icacheService;
    List<Utilities> utilityListAttach = new ArrayList<>();
    private EsUtilService esUtilService;
    private ParameterContext parameterContext;

    @Autowired
    private ExecutionDefaultRepository executionDefaultRepository;

    @Autowired
    private EsConfig esConfig;

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    @Autowired
    private EsUtilServiceImpl defaultEsUtilService;


    public ExecutionContextBean(JsonNode content, ApplicationContext applicationContext) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String contentSchema = content.get("schema").textValue();
        factoryContext.loadBeans(contentSchema,applicationContext);
        if(applicationContext.getAutowireCapableBeanFactory().containsBean(contentSchema)){
            Object obtainedBean = applicationContext.getAutowireCapableBeanFactory().getBean(contentSchema);
            setBeanName(String.valueOf(obtainedBean));
            Map<String,Object> beanMap =  ((FactoryBeanEntity) obtainedBean).getBeansMap();
            for (Map.Entry<String, Object> entry : beanMap.entrySet())
            {
                if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.VALIDATOR_APPEND)){
                    if(null != entry.getValue()){
                        setValidationService((ValidationService) entry.getValue());
                    }else{
                        setValidationService(new ValidationServiceDefaultImpl((ExecutionDefaultRepository) applicationContext.getAutowireCapableBeanFactory().getBean("executionDefaultRepository")));
                    }
                    setEntityRepository((JpaRepository) validationService.getRepository());
                }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.EXECUTOR_APPEND)){
                    if(null != entry.getValue()){
                        setExecutionService((ExecutionService) entry.getValue());
                    }else{
                        setExecutionService(new ExecutionServiceDefaultImpl());
                    }
                }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.READSAVE_APPEND)){
                    if(null != entry.getValue()){
                        setReadSaveStrategyService((ReadSaveStartegyService)entry.getValue());
                    }else{
                        setReadSaveStrategyService(new DefaultReadSaveStrategyImpl());
                    }
                }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.TRANSFORMER_APPEND)){
                    if(null != entry.getValue()){
                        setDataTransformerService((DataTransformerService)entry.getValue());
                    }else{
                        setDataTransformerService(new DefaultDataTransformerServiceImpl());
                    }
                }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.CACHE_APPEND)){
                    if(null != entry.getValue()){
                        setIcacheService((ICacheService) entry.getValue());
                    }else{
                        setIcacheService(new CacheService());
                    }
                }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.PARAM_APPEND)){
                    if(null != entry.getValue()){
                        setParameterContext((ParameterContext) entry.getValue());
                    }else{
                        setParameterContext(new DefaultParamaterContext(content));
                    }
                }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.CACHE_STRATEGY)){
                    if(null != entry.getValue()){
                        setCacheStrategyService((CacheStrategyService) entry.getValue());
                    }else{
                        setCacheStrategyService(new DefaultCacheServiceImpl());
                    }
                }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.UTILITY_APPEND)){
                    if(null != entry.getValue()){
                        setUtilityService((UtilityService) entry.getValue());
                    }else{
                        setUtilityService(new DefaultUtilityServiceImpl());
                    }
                }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.SEARCH_APPEND)){
                    if(null != entry.getValue()){
                        setEsUtilService((EsUtilService) entry.getValue());
                    }else{
                        setEsUtilService(new EsUtilServiceImpl((RestHighLevelClient) applicationContext.getAutowireCapableBeanFactory().getBean("elasticsearchClient"), (EsConfig) applicationContext.getAutowireCapableBeanFactory().getBean("esConfig")));
                    }
                }else if(entry.getKey().equalsIgnoreCase(contentSchema + Constants.UTILITIES_ATTACHMENT)){
                    if(null != entry.getValue()){
                        utilityListAttach.addAll((Collection<? extends Utilities>) entry.getValue());
                        setUtilityListAttach(utilityListAttach);
                    }
                }
            }
        }else{
            setValidationService(new ValidationServiceDefaultImpl((ExecutionDefaultRepository) applicationContext.getAutowireCapableBeanFactory().getBean("executionDefaultRepository")));
            setEntityRepository((JpaRepository) validationService.getRepository());
            setExecutionService(new ExecutionServiceDefaultImpl());
            setReadSaveStrategyService(new DefaultReadSaveStrategyImpl());
            setDataTransformerService(new DefaultDataTransformerServiceImpl());
            setIcacheService(new CacheService());
            setCacheStrategyService(new DefaultCacheServiceImpl());
            setUtilityService(new DefaultUtilityServiceImpl());
            setParameterContext(new DefaultParamaterContext(content));
            setEsUtilService(new EsUtilServiceImpl((RestHighLevelClient) applicationContext.getAutowireCapableBeanFactory().getBean("elasticsearchClient"), (EsConfig) applicationContext.getAutowireCapableBeanFactory().getBean("esConfig")));
        }

    }

    public void setId(String id) {
        this.id =  UUID.randomUUID().toString();
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setExecutionService(ExecutionService executionService) {
        this.executionService = executionService;
    }

    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    public void setReadSaveStrategyService(ReadSaveStartegyService readSaveStrategyService) {
        this.readSaveStrategyService = readSaveStrategyService;
    }

    public void setDataTransformerService(DataTransformerService dataTransformerService) {
        this.dataTransformerService = dataTransformerService;
    }

    public void setUtilityService(UtilityService utilityService) {
        this.utilityService = utilityService;
    }

    public void setCacheStrategyService(CacheStrategyService cacheStrategyService) {
        this.cacheStrategyService = cacheStrategyService;
    }

    public void setEntityRepository(JpaRepository entityRepository) {
        this.entityRepository = entityRepository;
    }

    public void setIcacheService(ICacheService icacheService) {
        this.icacheService = icacheService;
    }

    public void setUtilityListAttach(List<Utilities> utilityListAttach) {
        this.utilityListAttach = utilityListAttach;
    }

    public void setEsUtilService(EsUtilService esUtilService) {
        this.esUtilService = esUtilService;
    }

    public void setParameterContext(ParameterContext parameterContext) {
        this.parameterContext = parameterContext;
    }
}
