package com.searchspring.nextopia;

import java.io.StringWriter;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.searchspring.nextopia.model.SearchspringResponse;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

public class Converter {

    private final Gson GSON = new Gson();
    private final XStream xs = new XStream();

    public Converter() {
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

    public String convertNextopiaQueryUrl(String nextopiaQueryUrl) {
        return "newUrl";
    }

}