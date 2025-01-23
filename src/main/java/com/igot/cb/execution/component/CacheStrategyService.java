package com.igot.cb.execution.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.pores.cache.CacheService;
import com.igot.cb.pores.cache.ICacheService;
import com.igot.cb.pores.dto.CustomResponse;
import com.igot.cb.pores.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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