package com.searchspring.nextopia.model;

import java.util.Map;

public class SearchspringSearchResponse {
    public Pagination pagination;
    public Map<String,Object>[] results;
    public Map<String,Object>[] facets;
    public DidYouMean didYouMean;
}