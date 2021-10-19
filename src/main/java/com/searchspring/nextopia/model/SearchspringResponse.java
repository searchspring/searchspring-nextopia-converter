package com.searchspring.nextopia.model;

import java.util.Map;

public class SearchspringResponse {
    public Pagination pagination;
    public Map<String,Object>[] results;
    public Map<String,Object>[] facets;
}