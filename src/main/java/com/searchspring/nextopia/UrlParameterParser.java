package com.searchspring.nextopia;

import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlParameterParser {

    final Logger logger = LoggerFactory.getLogger(UrlParameterParser.class);

    public Map<String, String> parseQueryString(String queryString) {
        Map<String, String> queryMap = new LinkedHashMap<String, String>();
        if (queryString == null || queryString.toString().trim().length() == 0) {
            return queryMap;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx == -1) {
                continue;
            }
            String key = pair.substring(0, idx);
            String value = pair.substring(idx + 1);
            if (key.trim().length() == 0 || value.trim().length() == 0) {
                continue;
            }

            try {
                queryMap.put(URLDecoder.decode(key, "UTF-8"),
                        URLDecoder.decode(value, "UTF-8"));
            } catch (Exception e) {
                logger.error("Bad URL encoding", queryString);
            }
        }
        return queryMap;
    }
}