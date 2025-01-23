package com.igot.cb.execution.component;


import com.igot.cb.execution.basic.ExecutionDefaultRepository;
import com.igot.cb.pores.exceptions.CustomException;
import com.igot.cb.pores.util.Constants;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.InputStream;
import java.util.Set;

@Component
public interface ValidationService{

    default boolean validate(String fileName, JsonNode payload){
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

     Repository getRepository();

}