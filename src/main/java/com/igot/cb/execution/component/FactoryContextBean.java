package com.igot.cb.execution.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.pores.exceptions.CustomException;
import com.igot.cb.pores.util.Constants;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
public class FactoryContextBean {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    /**@PostConstruct
    public void afterAllBeansAreCreated() {
        // This will be called after regular Spring Boot beans are initialized
        System.out.println("All regular beans are initialized.");

        // Now access the lazy bean from the XML configuration
        Object myField = applicationContext.getBean("contentValidation");
        System.out.println("XML-configured bean loaded: " + myField);
    }**/


    public void loadBeans(String inputSchema) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ObjectMapper mapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource(Constants.PAYLOAD_FACTORY_CONTEXT_PROVIDER);
        JsonNode rootNode = mapper.readTree(resource.getInputStream());
        //JsonNode classNamesNode = rootNode.get("classNames");
        int count =0;

        if(rootNode.isArray()){
            for(JsonNode perNode : rootNode){

                //validate against the schema
                JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();
                InputStream schemaStream = schemaFactory.getClass().getResourceAsStream(Constants.PAYLOAD_FACTORY_CONTEXT_PROVIDER);
                JsonSchema schema = schemaFactory.getSchema(schemaStream);

                Set<ValidationMessage> validationMessages = schema.validate(perNode);
                if (!validationMessages.isEmpty()) {
                    StringBuilder errorMessage = new StringBuilder("Validation error(s): ");
                    for (ValidationMessage message : validationMessages) {
                        errorMessage.append(message.getMessage());
                    }
                    throw new CustomException(Constants.ERROR, errorMessage.toString(), HttpStatus.BAD_REQUEST);
                }else{
                    List<Object> objects = new ArrayList<>();
                    Map<String, Object> beansMap = new HashMap<>();

                    String factoryName = rootNode.get(count).get("factoryName").textValue();
                    if(inputSchema.equalsIgnoreCase(factoryName)) {
                        instantiateBean(factoryName + Constants.EXECUTOR_APPEND, rootNode.get(count).get("factoryExecutor").textValue(), beansMap);
                        instantiateBean(factoryName + Constants.VALIDATOR_APPEND, rootNode.get(count).get("factoryValidator").textValue(), beansMap);
                        instantiateBean(factoryName + Constants.READSAVE_APPEND, rootNode.get(count).get("factoryReadSave").textValue(), beansMap);
                        instantiateBean(factoryName + Constants.TRANSFORMER_APPEND, rootNode.get(count).get("factoryDataTransform").textValue(), beansMap);
                        instantiateBean(factoryName + Constants.UTILITY_APPEND, rootNode.get(count).get("factoryUtility").textValue(), beansMap);
                        instantiateBean(factoryName + Constants.SEARCH_APPEND, rootNode.get(count).get("factorySearch").textValue(), beansMap);
                        instantiateBean(factoryName + Constants.CACHE_APPEND, rootNode.get(count).get("factoryCache").textValue(), beansMap);
                        instantiateBean(factoryName + Constants.CACHE_STRATEGY, rootNode.get(count).get("factoryCacheStrategy").textValue(), beansMap);

                        instantiateUtilities(factoryName + Constants.UTILITIES_ATTACHMENT, rootNode.get(0).get("factoryUtilityAttachment"), beansMap);

                        //String factoryUtilityAttachment = String.valueOf()

                        // Create an instance of BeanHolder with the map
                        FactoryBeanEntity beanHolder = new FactoryBeanEntity(beansMap);

                        // Register the BeanHolder instance as a Spring bean
                        beanFactory.registerSingleton(factoryName, beanHolder);

                    }
                }

                count++;
            }
        }

    }

    public boolean checkFactory(String inputschema) throws IOException {
        boolean validFactory = false;
        ObjectMapper mapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource(Constants.PAYLOAD_FACTORY_CONTEXT_PROVIDER);
        JsonNode rootNode = mapper.readTree(resource.getInputStream());
        //JsonNode classNamesNode = rootNode.get("classNames");
        int count =0;

        if(rootNode.isArray()) {
            for (JsonNode perNode : rootNode) {

                //validate against the schema
                JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();
                InputStream schemaStream = schemaFactory.getClass().getResourceAsStream(Constants.PAYLOAD_FACTORY_CONTEXT_PROVIDER);
                JsonSchema schema = schemaFactory.getSchema(schemaStream);

                Set<ValidationMessage> validationMessages = schema.validate(perNode);
                if (!validationMessages.isEmpty()) {
                    StringBuilder errorMessage = new StringBuilder("Validation error(s): ");
                    for (ValidationMessage message : validationMessages) {
                        errorMessage.append(message.getMessage());
                    }
                    throw new CustomException(Constants.ERROR, errorMessage.toString(), HttpStatus.BAD_REQUEST);
                }else {
                    List<Object> objects = new ArrayList<>();
                    Map<String, Object> beansMap = new HashMap<>();

                    String factoryName = rootNode.get(count).get("factoryName").textValue();
                    if(inputschema.equalsIgnoreCase(factoryName)){
                        validFactory = true;
                    }
                }
            }
        }
        return validFactory;
    }


    private void instantiateUtilities(String factoryKey, JsonNode factoryCache, Map<String, Object> beansMap) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

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

    public void instantiateBean(String factoryKey, String factoryValue,Map<String, Object> beansMap) throws ClassNotFoundException {

        // Load class using reflection
        Class<?> runtimeClass = Class.forName(factoryValue);

        //create bean with the runtime class
        if(factoryKey.contains(Constants.ENTITY_APPEND)){
            Object beanInstance = applicationContext.getAutowireCapableBeanFactory().createBean(runtimeClass);
            beansMap.put(factoryKey, applicationContext.getBean(beanInstance.getClass()));
        }else{
            beansMap.put(factoryKey, applicationContext.getBean(runtimeClass));
        }

        //register the bean with application context
        //beanFactory.registerSingleton(factoryKey, beanInstance);

        // Create an instance of BeanHolder with the map
        //BeanHolder beanHolder = new BeanHolder(beansMap);

    }
}
