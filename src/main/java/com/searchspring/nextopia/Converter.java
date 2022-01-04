package com.searchspring.nextopia;

import static com.searchspring.nextopia.model.ParameterMappings.*;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.searchspring.nextopia.model.Product;
import com.searchspring.nextopia.model.SearchspringAutocompleteResponse;
import com.searchspring.nextopia.model.SearchspringSearchResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Converter {
    final Logger logger = LoggerFactory.getLogger(Converter.class);
    private final String SS_DOMAIN = ".a.searchspring.io";

    private final String SS_SEARCH_PATH = "/api/search/search.json";
    private final String SS_AUTOCOMPLETE_PATH = "/api/suggest/legacy";
    private final String SS_PRODUCT_COUNT = "productCount";

    private final String siteId;
    private final UrlParameterParser parser = new UrlParameterParser();
    private final Gson GSON = new Gson();

    public Converter(String siteId) {
        this.siteId = siteId;
    }

    public String convertNextopiaAutocompleteQueryUrl(String nextopiaQueryUrl) throws URISyntaxException {
        URI uri = new URI(nextopiaQueryUrl);
        Map<String, List<String>> queryMap = parser.parseQueryString(uri.getQuery());
        StringBuilder sb = createSearchspringAutocompleteUrl();
        mapParameter(sb, queryMap, NX_AUTOCOMPLETE_QUERY, SS_AUTOCOMPLETE_QUERY);
        logger.debug("Converted {} to {}", nextopiaQueryUrl, sb);
        return sb.toString();
    }

    public String convertSearchspringAutocompleteResponse(String callback, String searchspringResponse) {
        SearchspringAutocompleteResponse response = new SearchspringAutocompleteResponse();
        if (searchspringResponse != null) {
            response = GSON.fromJson(searchspringResponse, SearchspringAutocompleteResponse.class);
        }
        if (response == null) {
            response = new SearchspringAutocompleteResponse();
        }
        StringBuilder sb = new StringBuilder(callback + "(");
        appendTermsAndProducts(sb, response);
        sb.append(")");
        return sb.toString();
    }

    public void appendTermsAndProducts(StringBuilder sb, SearchspringAutocompleteResponse response) {
        Map<String, Object> container = new HashMap<>();
        Map<String, Object> terms = new HashMap<>();
        Map<String, Object> products = new HashMap<>();
        container.put("terms", terms);
        container.put("products", products);
        terms.put("n", "Popular Searches");
        if (response.terms != null) {
            terms.put("r", response.terms);
        }
        products.put("n", "Product Matches");
        if (response.products != null) {
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : response.products) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("Sku", product.sku);
                productMap.put("Url", product.url);
                productMap.put("Name", product.name);
                productMap.put("Price", product.price);
                productMap.put("Image", product.thumbnailImageUrl);
                productList.add(productMap);
            }
            products.put("r", productList);
        }
        sb.append(GSON.toJson(container));
    }

    public String convertSearchspringSearchResponse(String searchspringResponse) {
        SearchspringSearchResponse response = new SearchspringSearchResponse();
        if (searchspringResponse != null && !searchspringResponse.contains("\"results\":\"")) {
            response = GSON.fromJson(searchspringResponse, SearchspringSearchResponse.class);
        }
        if (response == null) {
            response = new SearchspringSearchResponse();
        }
        StringBuilder sb = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?><xml>");
        appendQueryTime(sb, response);
        appendSuggestSpelling(sb, response);
        appendCustomSynonyms(sb, response);
        appendPagination(sb, response);
        appendSearchedInField(sb);
        appendUserSearchDepth(sb);
        appendCurrentlySortedBy(sb);
        appendSortBys(sb);
        appendNotices(sb);
        appendMerchandizing(sb);
        appendRefinements(sb, response);
        appendResults(sb, response);
        appendIndexHash(sb);
        appendXmlFeedDone(sb);
        appendCl(sb);
        sb.append("</xml>");
        return sb.toString();
    }

    public String convertNextopiaSearchQueryUrl(String nextopiaQueryUrl) throws URISyntaxException {
        nextopiaQueryUrl = nextopiaQueryUrl.replaceAll("\\^", "%5E");
        URI uri = new URI(nextopiaQueryUrl);
        Map<String, List<String>> queryMap = parser.parseQueryString(uri.getQuery());
        StringBuilder sb = createSearchspringSearchUrl();
        mapParameter(sb, queryMap, NX_KEYWORDS, SS_KEYWORDS);
        mapParameter(sb, queryMap, NX_PAGE, SS_PAGE);
        mapParameter(sb, queryMap, NX_RES_PER_PAGE, SS_RES_PER_PAGE);
        mapRefinements(sb, queryMap);
        mapSort(sb, queryMap);
        logger.debug("Converted {} to {}", nextopiaQueryUrl, sb);
        return sb.toString();
    }

    private void appendSortBys(StringBuilder sb) {
        sb.append("<sort_bys/>");
    }

    private void appendCl(StringBuilder sb) {
        sb.append("<cl>0</cl>");
    }

    private void appendXmlFeedDone(StringBuilder sb) {
        sb.append("<xml_feed_done>1</xml_feed_done>");
    }

    private void appendIndexHash(StringBuilder sb) {
        sb.append("<index_hash><![CDATA[ aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa ]]></index_hash>");
    }

    private void appendMerchandizing(StringBuilder sb) {
        sb.append("<merchandizing/>");
    }

    private void appendUserSearchDepth(StringBuilder sb) {
        sb.append("<user_search_depth/>");
    }

    private void appendCurrentlySortedBy(StringBuilder sb) {
        sb.append("<currently_sorted_by/>");
    }

    private void appendSearchedInField(StringBuilder sb) {
        sb.append("<searched_in_field/>");
    }

    private void appendNotices(StringBuilder sb) {
        sb.append(
                "<notices><related_added><![CDATA[ 0 ]]></related_added><sku_match><![CDATA[ 0 ]]></sku_match><or_switch><![CDATA[ 0 ]]></or_switch></notices>");
    }

    private void appendCustomSynonyms(StringBuilder sb, SearchspringSearchResponse response) {
        sb.append("<custom_synonyms/>");
    }

    private void appendQueryTime(StringBuilder sb, SearchspringSearchResponse response) {
        sb.append("<query_time>0</query_time>");
    }

    private void appendSuggestSpelling(StringBuilder sb, SearchspringSearchResponse response) {
        sb.append("<suggested_spelling><![CDATA[");
        if (response.didYouMean != null && response.didYouMean.query != null) {
            sb.append(response.didYouMean.query);
        } else {
            sb.append(" ");
        }
        sb.append("]]></suggested_spelling>");
    }

    private void appendPagination(StringBuilder sb, SearchspringSearchResponse response) {
        sb.append("<pagination>");
        if (response.pagination != null) {
            sb.append("<total_products>").append(response.pagination.totalResults).append("</total_products>");
            sb.append("<product_min>").append(response.pagination.begin).append("</product_min>");
            sb.append("<product_max>").append(response.pagination.end).append("</product_max>");
            sb.append("<current_page>").append(response.pagination.currentPage).append("</current_page>");
            sb.append("<total_pages>").append(response.pagination.totalPages).append("</total_pages>");
            sb.append("<prev_page>").append(response.pagination.currentPage > 1 ? "1" : "0").append("</prev_page>");
            sb.append("<next_page>")
                    .append(response.pagination.currentPage < response.pagination.totalPages ? "1" : "0")
                    .append("</next_page>");
        } else {
            sb.append("<total_products>0</total_products>");
        }
        sb.append("</pagination>");
    }

    private void appendRefinements(StringBuilder sb, SearchspringSearchResponse response) {

        if (response.facets != null && response.facets.length > 0) {
            sb.append("<refinables>");

            for (Map<String, Object> facet : response.facets) {
                sb.append("<refinable>");
                sb.append("<name>").append("<![CDATA[").append(String.valueOf(facet.get("field"))).append("]]>")
                        .append("</name>");
                sb.append("<values>");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> values = (List<Map<String, Object>>) facet.get("values");
                for (Map<String, Object> value : values) {
                    sb.append("<value>");
                    sb.append("<name>").append("<![CDATA[").append(value.get("value")).append("]]>").append("</name>");
                    sb.append("<num>").append("<![CDATA[").append(doubleToInteger(value.get("count"))).append("]]>")
                            .append("</num>");
                    sb.append("</value>");
                }
                sb.append("</values>");
                sb.append("</refinable>");
            }
            sb.append("</refinables>");
        } else {
            sb.append("<refinables/>");
        }
    }

    private String doubleToInteger(Object object) {
        if (object == null || object.toString().trim().equals("")) {
            return "";
        }
        try {
            int result = (int) Double.parseDouble(object.toString());
            return String.valueOf(result);
        } catch (Exception e) {
            return "";
        }
    }

    private void appendResults(StringBuilder sb, SearchspringSearchResponse response) {
        if (response.results != null && response.results.length > 0) {
            sb.append("<results>");
            int counter = 0;
            for (Map<String, Object> result : response.results) {
                sb.append("<result>");
                sb.append("<rank>").append(String.valueOf(counter)).append("</rank>");
                sb.append("<Sku>").append("<![CDATA[").append(result.get("uid")).append("]]>").append("</Sku>");
                sb.append("<results_flags><![CDATA[attributized]]></results_flags>");
                sb.append("</result>");
                counter++;
            }
            sb.append("</results>");
        } else {
            sb.append("<results/>");
        }
    }

    private void mapSort(StringBuilder sb, Map<String, List<String>> queryMap) {
        List<String> sortValues = queryMap.get(NX_SORT_BY_FIELD);
        if (sortValues != null) {
            for (String sort : sortValues) {
                String[] fieldDirection = sort.split(":");
                if (fieldDirection.length == 2) {
                    sb.append("&").append("sort.").append(fieldDirection[0]).append("=")
                            .append(fieldDirection[1].toLowerCase());
                }
            }
        }
    }

    private void mapRefinements(StringBuilder sb, Map<String, List<String>> queryMap) {
        Map<String, List<String>> leftOverParameters = getLeftOverParameters(queryMap);
        Set<String> keySet = leftOverParameters.keySet();
        for (String key : keySet) {
            List<String> values = leftOverParameters.get(key);
            for (String value : values) {
                String[] ors = value.split("\\^");
                for (String or : ors) {
                    sb.append("&").append("filter.").append(key).append("=").append(encodeOrBlank(or));
                }
            }
        }
    }

    private String encodeOrBlank(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.warn("Couldn't encode parameter {}, {}", value, e);
        }
        return "";
    }

    private Map<String, List<String>> getLeftOverParameters(Map<String, List<String>> queryMap) {
        Map<String, List<String>> leftOverParameters = new LinkedHashMap<>();
        Set<String> keySet = queryMap.keySet();
        for (String key : keySet) {
            if (!ALL_NEXTOPIA_PARAMETERS.contains(key)) {
                leftOverParameters.put(key, queryMap.get(key));
            }
        }
        return leftOverParameters;
    }

    private StringBuilder createSearchspringSearchUrl() {
        return new StringBuilder("https://").append(siteId).append(SS_DOMAIN).append(SS_SEARCH_PATH).append("?")
                .append(SS_SITE_ID).append("=").append(siteId).append("&").append(SS_RESULTS_FORMAT).append("=json");
    }

    private StringBuilder createSearchspringAutocompleteUrl() {
        return new StringBuilder("https://").append(siteId).append(SS_DOMAIN).append(SS_AUTOCOMPLETE_PATH).append("?")
                .append(SS_SITE_ID).append("=").append(siteId).append("&").append(SS_PRODUCT_COUNT).append("=4");
    }

    private void mapParameter(StringBuilder sb, Map<String, List<String>> queryMap, String sourceParameter,
            String destinationParameter) {
        if (queryMap.containsKey(sourceParameter)) {
            List<String> values = queryMap.get(sourceParameter);
            for (String value : values) {
                sb.append("&").append(destinationParameter).append("=").append(encodeOrBlank(value));
            }
        }
    }

}