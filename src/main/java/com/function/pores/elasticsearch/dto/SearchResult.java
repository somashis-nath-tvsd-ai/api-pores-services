package com.function.pores.elasticsearch.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.function.execution.component.util.ESResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult implements Serializable, ESResult {

  private JsonNode data;
  private Map<String, List<FacetDTO>> facets;
  private long totalCount;
}
