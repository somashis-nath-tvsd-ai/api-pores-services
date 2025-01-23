package com.function.execution.basic;

import com.function.execution.component.core.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionDefaultRepository extends JpaRepository<ExecutionEntity,String> {
}
