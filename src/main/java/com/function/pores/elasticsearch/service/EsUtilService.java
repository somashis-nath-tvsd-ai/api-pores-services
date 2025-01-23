package com.function.pores.elasticsearch.service;

import com.function.execution.component.util.ESCriteria;
import com.function.execution.component.util.ESResult;
import com.function.execution.component.util.Utilities;

import java.io.InputStream;
import java.util.Map;

import com.function.pores.exceptions.CustomException;
import com.function.pores.util.Constants;
import com.networknt.schema.JsonSchemaFactory;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.http.HttpStatus;

public interface EsUtilService extends Utilities {

  ESCriteria getSearchCriteria();

  ESResult getSearchResult();

  RestHighLevelClient getElasticSearchClient();

  default boolean validateSearchSchema(String fileName){

    Boolean validate = false;

    try {
      JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();
      InputStream schemaStream = schemaFactory.getClass().getResourceAsStream("/EsFieldsmapping/"+fileName+".json");
      if(null!= schemaStream){
        validate = true;
      }

    } catch (CustomException e) {
      //logger.error("Failed to validate payload",e);
      throw new CustomException(Constants.ERROR, "Failed to validate payload: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return validate;

  }

  RestStatus addDocument(String esIndexName, String type, String id, Map<String, Object> document);

  RestStatus updateDocument(String index, String indexType, String entityId, Map<String, Object> document);

  void deleteDocument(String documentId, String esIndexName);

  void deleteDocumentsByCriteria(String esIndexName, SearchSourceBuilder sourceBuilder);

  ESResult searchDocuments(String esIndexName, ESCriteria searchCriteria) throws Exception;

}
