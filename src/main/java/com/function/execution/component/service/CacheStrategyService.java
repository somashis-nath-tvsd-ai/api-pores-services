package com.function.execution.component.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.function.pores.cache.CacheService;
import com.function.pores.cache.ICacheService;
import com.function.pores.dto.CustomResponse;
import org.springframework.stereotype.Component;

@Component
public interface CacheStrategyService {

    default Object dataFromCache(String id,ICacheService icacheService){
        CacheService localCacheService = (CacheService) icacheService;
        CustomResponse response = new CustomResponse();
        ObjectMapper objectMapper = new ObjectMapper();
        String cachedJson = localCacheService.getCache(id);
        return cachedJson;
    }

    default void dataSaveCache(String id, Object savedEntityObject, ICacheService icacheService){
        CacheService localCacheService = (CacheService) icacheService;
        localCacheService.putCache(id, savedEntityObject);
    }


}