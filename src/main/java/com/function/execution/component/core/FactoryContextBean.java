package com.function.execution.component.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.function.execution.basic.DefaultInitiateFactory;
import com.function.pores.exceptions.CustomException;
import com.function.pores.util.Constants;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
public class FactoryContextBean {


    @Autowired
    private ConfigurableBeanFactory beanFactory;

    private InitiateFactory initiateFactory = new DefaultInitiateFactory();

    /**@PostConstruct
    public void afterAllBeansAreCreated() {
        // This will be called after regular Spring Boot beans are initialized
        System.out.println("All regular beans are initialized.");

        // Now access the lazy bean from the XML configuration
        Object myField = applicationContext.getBean("contentValidation");
        System.out.println("XML-configured bean loaded: " + myField);
    }**/


    public void loadBeans(String inputSchema,ApplicationContext applicationContext) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
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

                    initiateFactory.loadSchemaBeans(rootNode,inputSchema,applicationContext,(ConfigurableBeanFactory)applicationContext.getAutowireCapableBeanFactory(),count);

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

}
