package com.searchspring.nextopia;

import static com.searchspring.nextopia.model.ParameterMappings.ALL_NEXTOPIA_PARAMETERS;
import static com.searchspring.nextopia.model.ParameterMappings.NX_KEYWORDS;
import static com.searchspring.nextopia.model.ParameterMappings.NX_PAGE;
import static com.searchspring.nextopia.model.ParameterMappings.NX_RES_PER_PAGE;
import static com.searchspring.nextopia.model.ParameterMappings.NX_SORT_BY_FIELD;
import static com.searchspring.nextopia.model.ParameterMappings.SS_KEYWORDS;
import static com.searchspring.nextopia.model.ParameterMappings.SS_PAGE;
import static com.searchspring.nextopia.model.ParameterMappings.SS_RESULTS_FORMAT;
import static com.searchspring.nextopia.model.ParameterMappings.SS_RES_PER_PAGE;
import static com.searchspring.nextopia.model.ParameterMappings.SS_SITE_ID;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.searchspring.nextopia.model.SearchspringResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Converter {
    final Logger logger = LoggerFactory.getLogger(Converter.class);
    private final String SS_DOMAIN = ".a.searchspring.io";

    private final String SS_PATH = "/api/search/search.json";

    private final String siteId;
    private final UrlParameterParser parser = new UrlParameterParser();
    private final Gson GSON = new Gson();
    private final static String EMPTY_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><pagination><total_products>0</total_products></pagination></xml>";

    public Converter(String siteId) {
        this.siteId = siteId;
    }

    public String convertSearchspringResponse(String searchspringResponse) {
        if (searchspringResponse.contains("\"results\":\"")) {
            return EMPTY_RESPONSE;
        }
        SearchspringResponse response = GSON.fromJson(searchspringResponse, SearchspringResponse.class);
        StringBuilder sb = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?><xml>");
        appendSuggestSpelling(sb, response);
        appendPagination(sb, response);
        appendRefinements(sb, response);
        appendResults(sb, response);
        sb.append("</xml>");
        return sb.toString();
    }

    private void appendSuggestSpelling(StringBuilder sb, SearchspringResponse response) {
        if (response.didYouMean != null && response.didYouMean.query != null) {
            sb.append("<suggested_spelling><![CDATA[").append(response.didYouMean.query)
                    .append("]]></suggested_spelling>");
        }
    }

    private void appendPagination(StringBuilder sb, SearchspringResponse response) {
        sb.append("<pagination>");
        sb.append("<total_products>").append(response.pagination.totalResults).append("</total_products>");
        sb.append("<product_min>").append(response.pagination.begin).append("</product_min>");
        sb.append("<product_max>").append(response.pagination.end).append("</product_max>");
        sb.append("<current_page>").append(response.pagination.currentPage).append("</current_page>");
        sb.append("<total_pages>").append(response.pagination.totalPages).append("</total_pages>");
        sb.append("<prev_page>").append(response.pagination.currentPage > 1 ? "1" : "0").append("</prev_page>");
        sb.append("<next_page>").append(response.pagination.currentPage < response.pagination.totalPages ? "1" : "0")
                .append("</next_page>");
        sb.append("</pagination>");
    }

    private void appendRefinements(StringBuilder sb, SearchspringResponse response) {
        sb.append("<refinables>");

        if (response.results != null && response.facets.length > 0) {

            for (Map<String, Object> facet : response.facets) {
                // // TODO replace with XML builder to avoid stringification issues
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
        }
        sb.append("</refinables>");
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

    private void appendResults(StringBuilder sb, SearchspringResponse response) {
        sb.append("<results>");
        if (response.results != null && response.results.length > 0) {
            int counter = 0;
            for (Map<String, Object> result : response.results) {
                // TODO replace with XML builder to avoid stringification issues
                sb.append("<result>");
                sb.append("<rank>").append(String.valueOf(counter)).append("</rank>");
                sb.append("<Sku>").append("<![CDATA[").append(result.get("uid")).append("]]>").append("</Sku>");
                sb.append("<results_flags><![CDATA[attributized]]></results_flags>");
                sb.append("</result>");
                counter++;
            }
        }
        sb.append("</results>");
    }

    public String convertNextopiaQueryUrl(String nextopiaQueryUrl) throws URISyntaxException {
        nextopiaQueryUrl = nextopiaQueryUrl.replaceAll("\\^", "%5E");
        URI uri = new URI(nextopiaQueryUrl);
        Map<String, List<String>> queryMap = parser.parseQueryString(uri.getQuery());
        StringBuilder sb = createSearchspringUrl();
        mapParameter(sb, queryMap, NX_KEYWORDS, SS_KEYWORDS);
        mapParameter(sb, queryMap, NX_PAGE, SS_PAGE);
        mapParameter(sb, queryMap, NX_RES_PER_PAGE, SS_RES_PER_PAGE);
        mapRefinements(sb, queryMap);
        mapSort(sb, queryMap);
        logger.debug("Converted {} to {}", nextopiaQueryUrl, sb);
        return sb.toString();
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

    private StringBuilder createSearchspringUrl() {
        return new StringBuilder("https://").append(siteId).append(SS_DOMAIN).append(SS_PATH).append("?")
                .append(SS_SITE_ID).append("=").append(siteId).append("&").append(SS_RESULTS_FORMAT).append("=json");
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