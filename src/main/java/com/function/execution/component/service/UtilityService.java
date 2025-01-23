package com.function.execution.component.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.function.execution.component.util.Utilities;
import com.function.pores.cache.ICacheService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UtilityService {

    public void preSaveUtility(List<Utilities> utilityList, Object dataObject);

    public void afterSaveUtility(List<Utilities> utilityList,Object dataObject);

    public void preUpdateUtility(List<Utilities> utilityList,Object dataObject);

    public void afterUpdateUtility(List<Utilities> utilityList,Object dataObject);

    default Object preReadUtility(List<Utilities> utilityList,Object dataObject){
        JsonNode jsonEntrity = (JsonNode) dataObject;
        String cachedJson = "";
        for(Object util: utilityList){
            Class<?> clazz = util.getClass();
            if(util instanceof ICacheService){
                ICacheService icacheService = (ICacheService)util;
                cachedJson = icacheService.getCache(String.valueOf(jsonEntrity.get("id")));
            }

        }

        return cachedJson;
    }

    public Object afterReadUtility(List<Utilities> utilityList,Object dataObject);

    public void preDeleteUtility(List<Utilities> utilityList,Object dataObject);

    public void afterDeleteUtility(List<Utilities> utilityList, Object dataObject);

}
