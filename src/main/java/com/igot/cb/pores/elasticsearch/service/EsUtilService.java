package com.igot.cb.pores.elasticsearch.service;

import com.igot.cb.execution.component.Utilities;
import com.igot.cb.pores.elasticsearch.dto.SearchCriteria;
import com.igot.cb.pores.elasticsearch.dto.SearchResult;
import java.util.Map;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public interface EsUtilService extends Utilities {
  
  RestStatus addDocument(String esIndexName, String type, String id, Map<String, Object> document);

  RestStatus updateDocument(String index, String indexType, String entityId, Map<String, Object> document);

  void deleteDocument(String documentId, String esIndexName);

  void deleteDocumentsByCriteria(String esIndexName, SearchSourceBuilder sourceBuilder);

  SearchResult searchDocuments(String esIndexName, SearchCriteria searchCriteria) throws Exception;

}
