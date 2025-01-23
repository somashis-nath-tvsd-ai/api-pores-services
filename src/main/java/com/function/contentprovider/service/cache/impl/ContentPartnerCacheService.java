package com.function.contentprovider.service.cache.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.function.execution.component.service.CacheStrategyService;
import com.function.pores.cache.CacheService;
import com.function.pores.cache.ICacheService;
import com.function.pores.dto.CustomResponse;
import com.function.pores.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service
public class ContentPartnerCacheService implements CacheStrategyService {
    @Override
    public CustomResponse dataFromCache(String id, ICacheService icacheService) {

        CacheService localCacheService = (CacheService) icacheService;
        CustomResponse response = new CustomResponse();
        ObjectMapper objectMapper = new ObjectMapper();
        String cachedJson = localCacheService.getCache(id);
        if (StringUtils.isNotEmpty(cachedJson)) {
            try {
                //log.info("Record coming from redis cache");
                response
                        .getResult()
                        .put(Constants.RESULT, objectMapper.readValue(cachedJson, new TypeReference<Object>() {
                        }));
            } catch (JsonProcessingException ex) {

            }
        }

        return response;
    }

    @Override
    public void dataSaveCache(String id, Object savedEntityObject, ICacheService icacheService) {

        CacheService localCacheService = (CacheService) icacheService;
        localCacheService.putCache(id, savedEntityObject);


    }

}
