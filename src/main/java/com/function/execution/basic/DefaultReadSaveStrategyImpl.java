package com.function.execution.basic;

import com.function.execution.component.service.ReadSaveStartegyService;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DefaultReadSaveStrategyImpl implements ReadSaveStartegyService {
    @Override
    public <T> List<T> dataFromDbUsingQueryParam(Map<String, Object> queryParam, Repository repository) {
        return List.of();
    }

    @Override
    public void deleteByMultipleParam(Map<String, Object> queryParam, Repository repository) {

    }
}
