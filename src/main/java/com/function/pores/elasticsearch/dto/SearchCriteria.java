package com.function.pores.elasticsearch.dto;

import java.util.HashMap;
import java.util.List;

import com.function.execution.component.util.ESCriteria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriteria implements ESCriteria {

  private HashMap<String, Object> filterCriteriaMap;

  private List<String> requestedFields;

  private int pageNumber;

  private int pageSize;

  private String orderBy;

  private String orderDirection;

  private String searchString;

  private List<String> facets;
}
