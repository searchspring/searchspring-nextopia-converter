package com.searchspring.nextopia;

import static com.searchspring.nextopia.model.ParameterMappings.ALL_NEXTOPIA_PARAMETERS;
import static com.searchspring.nextopia.model.ParameterMappings.NX_KEYWORDS;
import static com.searchspring.nextopia.model.ParameterMappings.SS_KEYWORDS;
import static com.searchspring.nextopia.model.ParameterMappings.SS_SITE_ID;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
        Map<String, String> queryMap = parser.parseQueryString(uri.getQuery());
        StringBuilder sb = createSearchspringUrl();
        mapParameter(sb, queryMap, NX_KEYWORDS, SS_KEYWORDS);
        mapRefinements(sb, queryMap);
        logger.debug("Converted {} to {}", nextopiaQueryUrl, sb);
        return sb.toString();
    }


    private void mapRefinements(StringBuilder sb, Map<String, String> queryMap) {
        Map<String, String> leftOverParameters = getLeftOverParameters(queryMap);
        Set<String> keySet = leftOverParameters.keySet();
        for (String key : keySet) {
            String value = leftOverParameters.get(key);
            try {
                sb.append("&").append("filter.").append(key).append("=").append(
                    URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.warn("Couldn't encode parameter {}, {}", value, e);
            }
        }
    }

    private Map<String, String> getLeftOverParameters(Map<String, String> queryMap) {
        Map<String, String> leftOverParameters = new TreeMap<>();
        Set<String> keySet = queryMap.keySet();
        for (String key : keySet) {
            if (!ALL_NEXTOPIA_PARAMETERS.contains(key)) {
                leftOverParameters.put(key, queryMap.get(key));
            }
        }
        System.out.println(leftOverParameters);
        return leftOverParameters;
    }

    private StringBuilder createSearchspringUrl() {
        return new StringBuilder("https://").append(siteId).append(SS_DOMAIN).append(SS_PATH).append("?")
                .append(SS_SITE_ID).append("=").append(siteId);
    }

    private void mapParameter(StringBuilder sb, Map<String, String> queryMap, String sourceParameter,
            String destinationParameter) {
        if (queryMap.containsKey(sourceParameter)) {
            sb.append("&");
            sb.append(destinationParameter).append("=").append(queryMap.get(sourceParameter));
        }
    }

}