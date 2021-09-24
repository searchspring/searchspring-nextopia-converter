package com.searchspring.nextopia;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.searchspring.nextopia.model.ParameterMappings;
import com.searchspring.nextopia.model.SearchspringResponse;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Converter {
    final Logger logger = LoggerFactory.getLogger(Converter.class);

    private final String siteId;
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
        Map<String, String> queryMap = parseQueryString(uri.getQuery());
        StringBuilder sb = new StringBuilder("http://").append(siteId).append(".a.searchspring.io/api?siteId=").append(siteId);
        mapParameter(sb, queryMap, ParameterMappings.NX_KEYWORDS, ParameterMappings.SS_KEYWORDS);
        logger.debug("Converted {} to {}", nextopiaQueryUrl, sb.toString());
        return sb.toString();
    }

    private void mapParameter(StringBuilder sb, Map<String, String> queryMap, String sourceParameter,
            String destinationParameter) {
        if (queryMap.containsKey(sourceParameter)) {
            sb.append("&");
            sb.append(destinationParameter).append("=").append(queryMap.get(sourceParameter));
        }
    }

    private Map<String, String> parseQueryString(String query) {
        Map<String, String> queryMap = new LinkedHashMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                queryMap.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                        URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // Should never happen.
                logger.error("Missing utf-8 encoding", e);
            }
        }
        return queryMap;
    }

}