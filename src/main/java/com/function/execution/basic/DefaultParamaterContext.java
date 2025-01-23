package com.function.execution.basic;

import com.fasterxml.jackson.databind.JsonNode;
import com.function.execution.component.api.ParameterContext;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultParamaterContext implements ParameterContext {

    private JsonNode data;


}
