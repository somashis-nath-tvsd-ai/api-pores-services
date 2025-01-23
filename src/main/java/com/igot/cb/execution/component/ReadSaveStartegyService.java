package com.igot.cb.execution.component;


import com.igot.cb.pores.cache.ICacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.pores.dto.CustomResponse;
import com.igot.cb.pores.util.Constants;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public interface ReadSaveStartegyService{

    default Object dataFromDBUsingPrimaryKey(String id, Repository repository){
        JpaRepository inlineRepository = (JpaRepository)repository;
        Optional<Object> content = inlineRepository.findById(id);
        //if(content != null){
        return Optional.of(content.get());
    }


    default CustomResponse dataFromCache(String id, ICacheService cacheService){
        CustomResponse response = new CustomResponse();
        ObjectMapper objectMapper = new ObjectMapper();
        String cachedJson = cacheService.getCache(id);
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

        return  response ; 
    }

    //default option for basic operation
    //open for next version
    <T>List<T> dataFromDbUsingQueryParam(Map<String, Object> queryParam, Repository repository);

    default Object saveData(Object queryParam, Repository repository){
        JpaRepository inlineRepository = (JpaRepository)repository;
        Object saveJsonEntity = inlineRepository.save(queryParam);

        return saveJsonEntity;
    }

    default void deleteByPrimaryKey(String id, Repository repository){
        JpaRepository inlineRepository = (JpaRepository)repository;
        inlineRepository.deleteById(id);
    }

    void deleteByMultipleParam(Map<String, Object> queryParam, Repository repository);

}