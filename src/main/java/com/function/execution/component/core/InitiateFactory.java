package com.function.execution.component.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.function.execution.component.util.Utilities;
import com.function.pores.exceptions.CustomException;
import com.function.pores.util.Constants;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface InitiateFactory {

    default void instantiateBean(String factoryKey, String factoryValue,
                                 Map<String, Object> beansMap,
                                 ApplicationContext applicationContext) throws ClassNotFoundException {
        if(factoryValue != null) {
            // Load class using reflection
            Class<?> runtimeClass = Class.forName(factoryValue);

            //create bean with the runtime class
            if (factoryKey.contains(Constants.ENTITY_APPEND)) {
                Object beanInstance = applicationContext.getAutowireCapableBeanFactory().createBean(runtimeClass);
                beansMap.put(factoryKey, applicationContext.getBean(beanInstance.getClass()));
            } else {
                beansMap.put(factoryKey, applicationContext.getBean(runtimeClass));
            }
        }else{
            beansMap.put(factoryKey, null);
        }

    }

    default void loadSchemaBeans(JsonNode rootNode, String inputSchema,
                                 ApplicationContext applicationContext,
                                 ConfigurableBeanFactory beanFactory,int count) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        List<Object> objects = new ArrayList<>();
        Map<String, Object> beansMap = new HashMap<>();

        String factoryName = rootNode.get(count).get("SchemaName").textValue();
        if(inputSchema.equalsIgnoreCase(factoryName)) {
            instantiateBean(factoryName + Constants.EXECUTOR_APPEND,(rootNode.get(count).get("ExecutionService") != null) ? rootNode.get(count).get("ExecutionService").textValue() : null , beansMap,applicationContext);
            instantiateBean(factoryName + Constants.VALIDATOR_APPEND,(rootNode.get(count).get("ValidationService") != null) ? rootNode.get(count).get("ValidationService").textValue() : null , beansMap,applicationContext);
            instantiateBean(factoryName + Constants.READSAVE_APPEND,(rootNode.get(count).get("ReadSaveStrategyService") != null) ? rootNode.get(count).get("ReadSaveStrategyService").textValue() : null , beansMap,applicationContext);
            instantiateBean(factoryName + Constants.TRANSFORMER_APPEND,(rootNode.get(count).get("DataTransformerService") != null) ? rootNode.get(count).get("DataTransformerService").textValue() : null , beansMap,applicationContext);
            instantiateBean(factoryName + Constants.UTILITY_APPEND,(rootNode.get(count).get("UtilityService") != null) ? rootNode.get(count).get("UtilityService").textValue() : null , beansMap,applicationContext);
            instantiateBean(factoryName + Constants.SEARCH_APPEND, (rootNode.get(count).get("SearchService") != null) ? rootNode.get(count).get("SearchService").textValue() : null, beansMap,applicationContext);
            instantiateBean(factoryName + Constants.CACHE_APPEND, (rootNode.get(count).get("CacheService") != null) ? rootNode.get(count).get("CacheService").textValue() : null , beansMap,applicationContext);
            instantiateBean(factoryName + Constants.CACHE_STRATEGY,(rootNode.get(count).get("CacheStrategyService") != null) ? rootNode.get(count).get("CacheStrategyService").textValue() : null , beansMap,applicationContext);
            instantiateBean(factoryName + Constants.PARAM_APPEND,(rootNode.get(count).get("ParameterContext") != null) ? rootNode.get(count).get("ParameterContext").textValue() : null , beansMap,applicationContext);
            instantiateUtilities(factoryName + Constants.UTILITIES_ATTACHMENT, rootNode.get(count).get("UtilityAttachment"), beansMap,applicationContext);

            //String factoryUtilityAttachment = String.valueOf()

            // Create an instance of BeanHolder with the map
            FactoryBeanEntity beanHolder = new FactoryBeanEntity(beansMap);

            // Register the BeanHolder instance as a Spring bean
            beanFactory.registerSingleton(factoryName, beanHolder);

        }

    }

    default void instantiateUtilities(String factoryKey, JsonNode factoryCache,
                                      Map<String, Object> beansMap
    ,ApplicationContext applicationContext) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        List<Object> utilitiesList = new ArrayList<>();

        if(factoryCache.isArray()){
            for(JsonNode utilNode : factoryCache){
                Class<?> runtimeClass = Class.forName(utilNode.textValue());
                Class<?> utilitiClass = Utilities.class;
                boolean runtimeInterface = false;
                if(utilitiClass.isAssignableFrom(runtimeClass)){
                    Object obj = applicationContext.getBean(runtimeClass);
                    utilitiesList.add(obj);
                    runtimeInterface = true;
                }
                if(!runtimeInterface){
                    StringBuilder errorMessage = new StringBuilder("Bean missing for utilities : ");
                    throw new CustomException(Constants.ERROR, errorMessage.toString(), HttpStatus.BAD_REQUEST);
                }
            }
        }

        beansMap.put(factoryKey,utilitiesList);

    }

}
