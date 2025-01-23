package com.igot.cb.contentprovider.service.utility.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.execution.component.Utilities;
import com.igot.cb.execution.component.UtilityService;
import com.igot.cb.pores.cache.ICacheService;
import com.igot.cb.pores.dto.CustomResponse;
import com.igot.cb.pores.elasticsearch.service.EsUtilService;
import com.igot.cb.pores.util.Constants;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class ContentPartnerUtilityService implements UtilityService {

    @Override
    public void preSaveUtility(List<Utilities> utilityList, Object dataObject) {

    }

    @Override
    public void afterSaveUtility(List<Utilities> utilityList, Object dataObject) {
        JsonNode jsonEntrity = (JsonNode) dataObject;
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.convertValue(jsonEntrity, Map.class);
        for(Utilities util: utilityList){

            if(util instanceof ICacheService){
                ICacheService icacheService = (ICacheService)util;
                icacheService.putCache(String.valueOf(jsonEntrity.get("id")), jsonEntrity);
            }else if(util instanceof EsUtilService){
                EsUtilService esUtiliService = (EsUtilService)util;
                esUtiliService.addDocument(Constants.INDEX_NAME, Constants.INDEX_TYPE, String.valueOf(jsonEntrity.get("id")), map);
            }

        }

    }

    @Override
    public void preUpdateUtility(List<Utilities> utilityList,Object dataObject) {

    }

    @Override
    public void afterUpdateUtility(List<Utilities> utilityList,Object dataObject) {

    }

    @Override
    public String preReadUtility(List<Utilities> utilityList,Object dataObject) {
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

    @Override
    public CustomResponse afterReadUtility(List<Utilities> utilityList,Object dataObject) {
        return null;
    }

    @Override
    public void preDeleteUtility(List<Utilities> utilityList,Object dataObject) {

    }

    @Override
    public void afterDeleteUtility(List<Utilities> utilityList,Object dataObject) {

    }
}
