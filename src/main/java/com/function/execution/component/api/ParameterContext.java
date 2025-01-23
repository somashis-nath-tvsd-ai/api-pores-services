package com.function.execution.component.api;

import com.fasterxml.jackson.databind.JsonNode;


public interface ParameterContext {


    default String getContentSchema(JsonNode data){
        return data.get("schema").textValue();
    }

    default JsonNode getContentData(JsonNode data){
        return data.get("data");
    }

    default String getESOption(JsonNode data){
        return data.get("search").textValue();
    }

    default Object validateParameter(JsonNode data){

        Boolean searchParam = false;
        if(null != data.get("search")){
            searchParam = true;
        }

        return searchParam;
    }

}
