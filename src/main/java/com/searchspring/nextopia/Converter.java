package com.searchspring.nextopia;

import static com.searchspring.nextopia.model.ParameterMappings.ALL_NEXTOPIA_PARAMETERS;
import static com.searchspring.nextopia.model.ParameterMappings.NX_KEYWORDS;
import static com.searchspring.nextopia.model.ParameterMappings.NX_PAGE;
import static com.searchspring.nextopia.model.ParameterMappings.NX_RES_PER_PAGE;
import static com.searchspring.nextopia.model.ParameterMappings.NX_SORT_BY_FIELD;
import static com.searchspring.nextopia.model.ParameterMappings.SS_KEYWORDS;
import static com.searchspring.nextopia.model.ParameterMappings.SS_PAGE;
import static com.searchspring.nextopia.model.ParameterMappings.SS_RES_PER_PAGE;
import static com.searchspring.nextopia.model.ParameterMappings.SS_SITE_ID;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.searchspring.nextopia.model.SearchspringResponse;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Converter {
    final Logger logger = LoggerFactory.getLogger(Converter.class);
    private final String SS_DOMAIN = ".a.searchspring.io";

    private final String SS_PATH = "/api";

    private final String siteId;
    private final UrlParameterParser parser = new UrlParameterParser();
    private final Gson GSON = new Gson();
    private final XStream xs = new XStream();

    public Converter(String siteId) {
        this.siteId = siteId;
        xs.registerConverter(new MapEntryConverter());
        xs.alias("result", LinkedTreeMap.class);
    }

    public String convertSearchspringResponse(String searchspringResponse) {
        SearchspringResponse response = GSON.fromJson(searchspringResponse, SearchspringResponse.class);
        StringBuilder sb = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><pagination><total_products>"
                        + getTotalResults(response) + "</total_products></pagination>");

        appendResults(sb, response);
        sb.append("</xml>");
        return sb.toString();
    }

    private void appendResults(StringBuilder sb, SearchspringResponse response) {
        StringWriter sw = new StringWriter();
        CompactWriter cw = new CompactWriter(sw);
        sb.append("<results>");
        if (response.results != null && response.results.length > 0) {
            int counter = 0;
            for (Map<String, Object> result : response.results) {
                result.put("rank", String.valueOf(counter));
                xs.marshal(result, cw);
                sb.append(sw.toString());
                counter++;
            }
        }
        sb.append("</results>");
    }

    private String getTotalResults(SearchspringResponse response) {
        if (response.pagination != null) {
            return String.valueOf(response.pagination.totalResults);
        }
        return "0";
    }

    public String convertNextopiaQueryUrl(String nextopiaQueryUrl) throws URISyntaxException {
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
        List<String> sortValues  = queryMap.get(NX_SORT_BY_FIELD);
        if (sortValues != null) {
            for (String sort : sortValues) {
                String[] fieldDirection = sort.split(":");
                if (fieldDirection.length == 2) {
                    sb.append("&").append("sort.").append(fieldDirection[0]).append("=").append(fieldDirection[1].toLowerCase());
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
                .append(SS_SITE_ID).append("=").append(siteId);
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