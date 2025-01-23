package com.igot.cb.contentprovider.service.readsavestrategy.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.igot.cb.contentprovider.repository.ContentPartnerRepository;
import com.igot.cb.execution.component.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import com.igot.cb.execution.component.ReadSaveStartegyService;
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
        List<ExecutionEntity> contentPartnerEntity = inlineRepository.findByFirstNameAndDepartment(String.valueOf(queryParam.get("role")), String.valueOf(queryParam.get("link")));
        return (List<T>) contentPartnerEntity;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object saveData(Object queryParam, Repository repository) {

        JpaRepository inlineRepository = (JpaRepository)repository;
        Object saveJsonEntity = inlineRepository.save(queryParam);

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
        inlineRepository.deleteByMultipleParams(String.valueOf(queryParam.get("role")), String.valueOf(queryParam.get("contentPartnerName")));

    }


}