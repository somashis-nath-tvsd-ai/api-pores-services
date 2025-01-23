package com.function.execution.basic;

import com.function.execution.component.service.ValidationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ValidationServiceDefaultImpl implements ValidationService {

    @Autowired
    private ExecutionDefaultRepository executionDefaultRepository;

    @Override
    public Repository getRepository() {
        return executionDefaultRepository;
    }
}
