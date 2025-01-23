package com.function.pores.elasticsearch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.function.execution.component.util.ESCriteria;
import com.function.execution.component.util.ESResult;
import com.function.pores.elasticsearch.config.EsConfig;
import com.function.pores.elasticsearch.dto.FacetDTO;
import com.function.pores.elasticsearch.dto.SearchCriteria;
import com.function.pores.elasticsearch.dto.SearchResult;
import com.function.pores.util.Constants;
import com.networknt.schema.JsonSchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;

@Service
@Slf4j
public class EsUtilServiceImpl implements EsUtilService {

  /*@Autowired
  private RestHighLevelClient elasticsearchClient;*/
  private final EsConfig esConfig;
  private final RestHighLevelClient elasticsearchClient;

  private Map<String, String> indexFields =  new LinkedHashMap<String,String>();

  @Getter
  private SearchCriteria searchCriteria;

  @Getter
  private SearchResult searchResult;

  @Autowired
  private ObjectMapper objectMapper;

  public EsUtilServiceImpl(RestHighLevelClient elasticsearchClient, EsConfig esConnection) {
    this.elasticsearchClient = elasticsearchClient;
    this.esConfig = esConnection;
  }

  @Value("${elastic.required.field.json.path}")
  private String requiredJsonFilePath;


  @Override
  public ESCriteria getSearchCriteria() {
    return searchCriteria;
  }

  @Override
  public ESResult getSearchResult() {
    return searchResult;
  }

  @Override
  public RestHighLevelClient getElasticSearchClient() {
    return elasticsearchClient;
  }

  @Override
  public RestStatus addDocument(
          String esIndexName, String type, String id, Map<String, Object> document) {
    try {
      objectMapper =new ObjectMapper();
      JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();
      InputStream schemaStream = schemaFactory.getClass().getResourceAsStream("/EsFieldsmapping/"+esIndexName+".json");
      Map<String, Object> map = objectMapper.readValue(schemaStream,
              new TypeReference<Map<String, Object>>() {
              });
      Iterator<Entry<String, Object>> iterator = document.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<String, Object> entry = iterator.next();
        String key = entry.getKey();
        if (!map.containsKey(key)) {
          //iterator.remove();
        }
      }
      IndexRequest indexRequest =
              new IndexRequest(esIndexName, type, id).source(document, XContentType.JSON);
      IndexResponse response = elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
      return response.status();
    } catch (Exception e) {
      log.error("Issue while Indexing to es: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public RestStatus updateDocument(
          String esIndexName, String indexType, String entityId, Map<String, Object> updatedDocument) {
    try {
      objectMapper =new ObjectMapper();
      JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();
      InputStream schemaStream = schemaFactory.getClass().getResourceAsStream("/EsFieldsmapping/"+esIndexName+".json");
      Map<String, Object> map = objectMapper.readValue(schemaStream,
              new TypeReference<Map<String, Object>>() {
              });
      Iterator<Entry<String, Object>> iterator = updatedDocument.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<String, Object> entry = iterator.next();
        String key = entry.getKey();
        if (!map.containsKey(key)) {
          //iterator.remove();
        }
      }
      IndexRequest indexRequest =
              new IndexRequest(esIndexName)
                      .id(entityId)
                      .source(updatedDocument)
                      .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
      IndexResponse response = elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
      return response.status();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public void deleteDocument(String documentId, String esIndexName) {
    try {
      DeleteRequest request = new DeleteRequest(esIndexName, Constants.INDEX_TYPE, documentId);
      DeleteResponse response = elasticsearchClient.delete(request, RequestOptions.DEFAULT);
      if (response.getResult() == DocWriteResponse.Result.DELETED) {
        log.info("Document deleted successfully from elasticsearch.");
      } else {
        log.error("Document not found or failed to delete from elasticsearch.");
      }
    } catch (Exception e) {
      log.error("Error occurred during deleting document in elasticsearch");
    }
  }

  @Override
  public SearchResult searchDocuments(String esIndexName, ESCriteria exCriteria) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    SearchCriteria searchCriteria = (SearchCriteria)exCriteria;
    getIndexFields(esIndexName);
    SearchSourceBuilder searchSourceBuilder = buildSearchSourceBuilder(searchCriteria,esIndexName);
    SearchRequest searchRequest = new SearchRequest(esIndexName);
    searchRequest.source(searchSourceBuilder);
    try {
      if (searchSourceBuilder != null) {
        int pageNumber = searchCriteria.getPageNumber();
        int pageSize = searchCriteria.getPageSize();
        searchSourceBuilder.from(pageNumber);
        if (pageSize != 0) {
          searchSourceBuilder.size(pageSize);
        }
      }
      SearchResponse paginatedSearchResponse =
              elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
      List<Map<String, Object>> paginatedResult = extractPaginatedResult(paginatedSearchResponse);
      Map<String, List<FacetDTO>> fieldAggregations = extractFacetData(paginatedSearchResponse, searchCriteria);
      SearchResult searchResult = new SearchResult();
      searchResult.setData(objectMapper.valueToTree(paginatedResult));
      searchResult.setFacets(fieldAggregations);
      searchResult.setTotalCount(paginatedSearchResponse.getHits().getTotalHits().value);
      return searchResult;
    } catch (IOException e) {
      log.error("Error while fetching details from elastic search");
      return null;
    }
  }

  public Map<String, List<FacetDTO>> extractFacetData(
          SearchResponse searchResponse, SearchCriteria searchCriteria) {
    Map<String, List<FacetDTO>> fieldAggregations = new HashMap<>();
    if (searchCriteria.getFacets() != null) {
      for (String field : searchCriteria.getFacets()) {
        if(null != searchResponse.getAggregations()) {
          Terms fieldAggregation = searchResponse.getAggregations().get(field + "_agg");
          List<FacetDTO> fieldValueList = new ArrayList<>();
          for (Terms.Bucket bucket : fieldAggregation.getBuckets()) {
            if (!bucket.getKeyAsString().isEmpty()) {
              FacetDTO facetDTO = new FacetDTO(bucket.getKeyAsString(), bucket.getDocCount());
              fieldValueList.add(facetDTO);
            }
          }
          fieldAggregations.put(field, fieldValueList);
        }
      }
    }
    return fieldAggregations;
  }

  public List<Map<String, Object>> extractPaginatedResult(SearchResponse paginatedSearchResponse) {
    SearchHit[] hits = paginatedSearchResponse.getHits().getHits();
    List<Map<String, Object>> paginatedResult = new ArrayList<>();
    for (SearchHit hit : hits) {
      paginatedResult.add(hit.getSourceAsMap());
    }
    return paginatedResult;
  }

  public SearchSourceBuilder buildSearchSourceBuilder(SearchCriteria searchCriteria, String esIndexName) throws IOException {
    log.info("Building search query");
    if (searchCriteria == null || searchCriteria.toString().isEmpty()) {
      log.error("Search criteria body is missing");
      return null;
    }
    BoolQueryBuilder boolQueryBuilder = buildFilterQuery(searchCriteria.getFilterCriteriaMap());
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(boolQueryBuilder);
    addSortToSearchSourceBuilder(searchCriteria, searchSourceBuilder);
    addRequestedFieldsToSearchSourceBuilder(searchCriteria, searchSourceBuilder,esIndexName);
    addQueryStringToFilter(searchCriteria, boolQueryBuilder, esIndexName);
    addFacetsToSearchSourceBuilder(searchCriteria.getFacets(), searchSourceBuilder);
    return searchSourceBuilder;
  }

  public BoolQueryBuilder buildFilterQuery(Map<String, Object> filterCriteriaMap) {
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    if (filterCriteriaMap != null) {
      filterCriteriaMap.forEach(
              (field, value) -> {
                if (value instanceof Boolean) {
                  boolQueryBuilder.must(QueryBuilders.termQuery(field, value));
                } else if (value instanceof ArrayList) {
                  if(indexFields.get(field).equalsIgnoreCase("text")){
                    boolQueryBuilder.must(
                            QueryBuilders.termsQuery(
                                    field+Constants.KEYWORD, ((ArrayList<?>) value).toArray()));
                  }else if(indexFields.get(field).equalsIgnoreCase("keyword")){
                    boolQueryBuilder.must(
                            QueryBuilders.termsQuery(
                                    field, ((ArrayList<?>) value).toArray()));
                  }

                } else if (value instanceof String) {
                  if(indexFields.get(field).equalsIgnoreCase("text")){
                    boolQueryBuilder.must(QueryBuilders.termsQuery(field+Constants.KEYWORD, value));
                  }else if(indexFields.get(field).equalsIgnoreCase("keyword")){
                    boolQueryBuilder.must(QueryBuilders.termsQuery(field, value));
                  }

                }
              });
    }
    return boolQueryBuilder;
  }

  public void addSortToSearchSourceBuilder(
          SearchCriteria searchCriteria, SearchSourceBuilder searchSourceBuilder) {
    if (isNotBlank(searchCriteria.getOrderBy()) && isNotBlank(searchCriteria.getOrderDirection())) {

      if(indexFields.get(searchCriteria.getOrderBy()).equalsIgnoreCase("text")){
        SortOrder sortOrder =
                Constants.ASC.equals(searchCriteria.getOrderDirection()) ? SortOrder.ASC : SortOrder.DESC;
        searchSourceBuilder.sort(
                SortBuilders.fieldSort(searchCriteria.getOrderBy()+Constants.KEYWORD)
                        .order(sortOrder));
      }else if(indexFields.get(searchCriteria.getOrderBy()).equalsIgnoreCase("keyword")){
        SortOrder sortOrder =
                Constants.ASC.equals(searchCriteria.getOrderDirection()) ? SortOrder.ASC : SortOrder.DESC;
        searchSourceBuilder.sort(
                SortBuilders.fieldSort(searchCriteria.getOrderBy())
                        .order(sortOrder));
      }


    }
  }

  private void addRequestedFieldsToSearchSourceBuilder(
          SearchCriteria searchCriteria, SearchSourceBuilder searchSourceBuilder, String esIndexName) throws IOException {
    if (searchCriteria.getRequestedFields() == null) {
      // Get all fields in response
      searchSourceBuilder.fetchSource(null);
    } else {
      if (searchCriteria.getRequestedFields().isEmpty()) {
        searchSourceBuilder.fetchSource(null);
        //log.error("Please specify at least one field to include in the results.");
      }else{
        List<String> requestFields = searchCriteria.getRequestedFields();
        ListIterator<String> reqFiledItr = requestFields.listIterator();
        while(reqFiledItr.hasNext()){
          String reqItrVal = reqFiledItr.next();
          if(indexFields.get(reqItrVal).equalsIgnoreCase("text")){
            reqFiledItr.set(reqItrVal+Constants.KEYWORD);
          }
        }
        searchSourceBuilder.fetchSource(
                requestFields.toArray(new String[0]), null);
      }

    }
  }

    private void addQueryStringToFilter(SearchCriteria searchCriteria, BoolQueryBuilder boolQueryBuilder, String esIndexName) throws IOException {
      List<String> searchFields = searchCriteria.getRequestedFields();
      BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

      if (searchFields.isEmpty()) {

        for (Entry<String, String> entry : indexFields.entrySet()){

            String entryVal = entry.getValue();
          if(entryVal.equalsIgnoreCase("text")){
            boolQuery.should(new WildcardQueryBuilder(entry.getKey()+Constants.KEYWORD,"*"+searchCriteria.getSearchString()+"*"));
          }else if(entryVal.equalsIgnoreCase("keyword")){
            boolQuery.should(new WildcardQueryBuilder(entry.getKey(),"*"+searchCriteria.getSearchString()+"*"));
          }
        }
      }else{

        List<String> requestFields = searchCriteria.getRequestedFields();
        ListIterator<String> reqFiledItr = requestFields.listIterator();
        while(reqFiledItr.hasNext()){
          String reqItrVal = reqFiledItr.next();
          if(indexFields.get(reqItrVal).equalsIgnoreCase("text")){
            boolQuery.should(new WildcardQueryBuilder(reqItrVal+Constants.KEYWORD,"*"+searchCriteria.getSearchString()+"*"));
          }else if(indexFields.get(reqItrVal).equalsIgnoreCase("keyword")){
            boolQuery.should(new WildcardQueryBuilder(reqItrVal,"*"+searchCriteria.getSearchString()+"*"));
          }
        }


      }

      if (isNotBlank(searchCriteria.getSearchString())) {
        boolQueryBuilder.must(boolQuery);
      }


    }

    private void addFacetsToSearchSourceBuilder(
            List<String> facets, SearchSourceBuilder searchSourceBuilder) {
      if (facets != null) {
        for (String field : facets) {

          if(indexFields.get(field).equalsIgnoreCase("text")){
            searchSourceBuilder.aggregation(
                    AggregationBuilders.terms(field + "_agg").field(field+ ".keyword")
                            .size(250));
          }else{
            searchSourceBuilder.aggregation(
                    AggregationBuilders.terms(field + "_agg").field(field)
                            .size(250));
          }


        }
      }
    }

    private boolean isNotBlank(String value) {
      return value != null && !value.trim().isEmpty();
    }

    @Override
    public void deleteDocumentsByCriteria(String esIndexName, SearchSourceBuilder sourceBuilder) {
      try {
        SearchHits searchHits = executeSearch(esIndexName, sourceBuilder);
        if (searchHits.getTotalHits().value > 0) {
          BulkResponse bulkResponse = deleteMatchingDocuments(esIndexName, searchHits);
          if (!bulkResponse.hasFailures()) {
            log.info("Documents matching the criteria deleted successfully from Elasticsearch.");
          } else {
            log.error("Some documents failed to delete from Elasticsearch.");
          }
        } else {
          log.info("No documents match the criteria.");
        }
      } catch (Exception e) {
        log.error("Error occurred during deleting documents by criteria from Elasticsearch.", e);
      }
    }

//  @Override
//  public SearchResult searchDocuments(String esIndexName, ESCriteria searchCriteria) throws Exception {
//    return null;
//  }

    private SearchHits executeSearch(String esIndexName, SearchSourceBuilder sourceBuilder)
      throws IOException {
      SearchRequest searchRequest = new SearchRequest(esIndexName);
      searchRequest.source(sourceBuilder);
      SearchResponse searchResponse =
              elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
      return searchResponse.getHits();
    }

    private BulkResponse deleteMatchingDocuments(String esIndexName, SearchHits searchHits)
      throws IOException {
      BulkRequest bulkRequest = new BulkRequest();
      searchHits.forEach(
              hit -> bulkRequest.add(new DeleteRequest(esIndexName, Constants.INDEX_TYPE, hit.getId())));
      return elasticsearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }


    //public void filterKeyWord()
    public void getIndexFields(String indexName) throws IOException {

      RestClient lowLevelClient = elasticsearchClient.getLowLevelClient();

      // Create the request for the index mapping
      Request requestURL = new Request("GET", "/" + indexName + "/_mapping");

      // Execute the request
      Response response = lowLevelClient.performRequest(requestURL);
      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> responseMap = objectMapper.readValue(response.getEntity().getContent(), Map.class);
      Map<String, Object> indexMappings = (Map<String, Object>) responseMap.get(indexName);
      Map<String, Object> tempIndexMap = (Map<String, Object>) ((Map<String, Object>) indexMappings.get("mappings")).get("properties");

      for (Entry<String, Object> entry : tempIndexMap.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        System.out.println(key + " : " + value);
        if(((LinkedHashMap) value).containsKey("type")){
          indexFields.put(key, (String) ((LinkedHashMap) value).get("type"));
        }else if(((LinkedHashMap) value).containsKey("properties")){
          LinkedHashMap<String, Object> innerPropMap = (LinkedHashMap) ((LinkedHashMap) value).get("properties");
          for (Entry<String, Object> innerEntr : innerPropMap.entrySet()) {

            String keyIntr = innerEntr.getKey();
            Object valueIntr = innerEntr.getValue();
            System.out.println(keyIntr + " : " + valueIntr);
            if(((LinkedHashMap) valueIntr).containsKey("type")){
              //if(((LinkedHashMap) valueIntr).get("type").equals("keyword")){
                indexFields.put(key+"."+keyIntr, (String) ((LinkedHashMap) valueIntr).get("type"));
              //}
            }
          }

        }
        //for(String searchField : propertiesSet){
        //boolQuery.should(new WildcardQueryBuilder(key+Constants.KEYWORD,"*"+searchCriteria.getSearchString()+"*"));
        //}
      }

    }

  }

