package com.igot.cb.execution.basic;

import com.igot.cb.execution.component.ExecutionEntity;
import com.igot.cb.execution.component.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

@Component
public class ValidationServiceDefaultImpl implements ValidationService {

    @Autowired
    private ExecutionDefaultRepository executionDefaultRepository;

    @Override
    public Repository getRepository() {
        return executionDefaultRepository;
    }
}
