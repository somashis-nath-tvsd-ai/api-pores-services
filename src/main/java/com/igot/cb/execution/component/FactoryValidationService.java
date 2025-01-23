package com.igot.cb.execution.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.igot.cb.pores.dto.CustomResponse;
import com.igot.cb.pores.exceptions.CustomException;
import com.igot.cb.pores.util.Constants;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Set;


public interface FactoryValidationService {

    default CustomResponse createUpdateValidation(JsonNode payload){
        CustomResponse response = new CustomResponse();

        if (!payload.has("schema")) {
            response.setMessage("Schema is not present");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }else if (!payload.has("data")) {
            response.setMessage("Data is not present");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }else{
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    default CustomResponse readValidation(JsonNode payload){
        CustomResponse response = new CustomResponse();

        if (!payload.has("schema")) {
            response.setMessage("Schema is not present");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }else if (!payload.has("data")) {
            response.setMessage("Data is not present");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }else{
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    default CustomResponse deleteValidation(JsonNode payload){
        CustomResponse response = new CustomResponse();

        if (!payload.has("schema")) {
            response.setMessage("Schema is not present");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }else if (!payload.has("data")) {
            response.setMessage("Data is not present");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }else{
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    default boolean validateSchema(String fileName, JsonNode payload){
        Boolean validate = false;

        try {
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();
            InputStream schemaStream = schemaFactory.getClass().getResourceAsStream("/payloadValidation/"+fileName+".json");
            if(null!= schemaStream){
                    validate = true;
            }

        } catch (CustomException e) {
            //logger.error("Failed to validate payload",e);
            throw new CustomException(Constants.ERROR, "Failed to validate payload: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return validate;
    }
}
