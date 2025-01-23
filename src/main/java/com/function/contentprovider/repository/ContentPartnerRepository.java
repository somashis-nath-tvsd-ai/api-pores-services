package com.function.contentprovider.repository;

import com.function.execution.component.core.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ContentPartnerRepository extends JpaRepository<ExecutionEntity, String>{

    @Modifying
    @Transactional
    //@Query(value = "DELETE FROM ContentPartnerEntity e WHERE e.data->'role' = :role AND e.data->'link' = :link")
    //@Query(value = "DELETE FROM public.content WHERE data->>'role' = :role AND data->>'link' = :link", nativeQuery = true)
    @Query("DELETE FROM ExecutionEntity c WHERE jsonb_extract_path_text(c.data, 'serviceType') = ?1 AND jsonb_extract_path_text(c.data, 'serviceDate') = ?2")
    void deleteByMultipleParams(@Param("serviceType") String role, @Param("serviceDate") String contentPartnerName);

    //@Query(value = "SELECT id, created_on, data->>'role' AS role, updated_on FROM public.content ")
    //@Query(value = "SELECT id, created_on, data->'role' AS Role, data->'link' AS Link, updated_on " +
    //        "FROM public.content " +
    //        "WHERE data->>'role' = :role AND data->>'link' = :link", nativeQuery = true)
    @Query("SELECT c FROM ExecutionEntity c WHERE jsonb_extract_path_text(c.data, 'serviceType') = ?1 AND jsonb_extract_path_text(c.data, 'serviceDate') = ?2")
    List<ExecutionEntity> findByFirstNameAndDepartment(@Param("serviceType")
     String role, @Param("serviceDate")
     String link);

}