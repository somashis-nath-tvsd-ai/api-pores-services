package com.igot.cb.execution.basic;

import com.igot.cb.execution.component.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionDefaultRepository extends JpaRepository<ExecutionEntity,String> {
}
