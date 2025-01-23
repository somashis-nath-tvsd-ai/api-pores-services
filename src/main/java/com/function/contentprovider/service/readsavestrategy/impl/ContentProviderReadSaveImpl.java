package com.function.contentprovider.service.readsavestrategy.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.function.contentprovider.repository.ContentPartnerRepository;
import com.function.execution.component.core.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import com.function.execution.component.service.ReadSaveStartegyService;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;


@Service
public class ContentProviderReadSaveImpl implements  ReadSaveStartegyService{

    @Override
    public Optional<ExecutionEntity> dataFromDBUsingPrimaryKey(String id, Repository repository) {

        JpaRepository inlineRepository = (JpaRepository)repository;
        Optional<ExecutionEntity> content = inlineRepository.findById(id);
        //if(content != null){
            return Optional.of(content.get());
        //}
    }

    //open for next version
    @Override
    public <T>List<T> dataFromDbUsingQueryParam(Map<String, Object> queryParam, Repository repository) {


        ContentPartnerRepository inlineRepository = (ContentPartnerRepository)repository;
        List<ExecutionEntity> contentPartnerEntity = inlineRepository.findByFirstNameAndDepartment(String.valueOf(queryParam.get("serviceType")), String.valueOf(queryParam.get("serviceDate")));
        return (List<T>) contentPartnerEntity;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ExecutionEntity saveData(Object queryParam, Repository repository) {

        JpaRepository inlineRepository = (JpaRepository)repository;
        ExecutionEntity saveJsonEntity = (ExecutionEntity) inlineRepository.save(queryParam);

        return saveJsonEntity;
    }

    @Override
    public void deleteByPrimaryKey(String id, Repository repository) {
        JpaRepository inlineRepository = (JpaRepository)repository;
        inlineRepository.deleteById(id);

    }


    @Override
    public void deleteByMultipleParam(Map<String, Object> queryParam, Repository repository) {

        ContentPartnerRepository inlineRepository = (ContentPartnerRepository)repository;
        inlineRepository.deleteByMultipleParams(String.valueOf(queryParam.get("serviceType")), String.valueOf(queryParam.get("serviceDate")));

    }


}