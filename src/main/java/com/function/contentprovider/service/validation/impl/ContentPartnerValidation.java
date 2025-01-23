package com.function.contentprovider.service.validation.impl;

import java.io.InputStream;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.function.execution.component.service.ValidationService;
import com.function.contentprovider.repository.ContentPartnerRepository;
import com.function.pores.exceptions.CustomException;
import com.function.pores.util.Constants;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Service;


@Service
public class ContentPartnerValidation implements ValidationService{

    @Autowired
    private ContentPartnerRepository entityRepository;

    @Override
    public ContentPartnerRepository getRepository(){
        return entityRepository;
    }

    @Override
    public boolean validate(String fileName, JsonNode payload){

        Boolean validate = false;
        
        try {
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();
            InputStream schemaStream = schemaFactory.getClass().getResourceAsStream("/payloadValidation/"+fileName+".json");
            JsonSchema schema = schemaFactory.getSchema(schemaStream);

            Set<ValidationMessage> validationMessages = schema.validate(payload);
            if (!validationMessages.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Validation error(s): ");
                for (ValidationMessage message : validationMessages) {
                    errorMessage.append(message.getMessage());
                }
                //throw new CustomException(Constants.ERROR, errorMessage.toString(), HttpStatus.BAD_REQUEST);
            }else{
                validate = true;
            }
        } catch (CustomException e) {
            //logger.error("Failed to validate payload",e);
            throw new CustomException(Constants.ERROR, "Failed to validate payload: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return validate;
    }


}