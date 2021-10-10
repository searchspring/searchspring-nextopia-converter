package com.searchspring.nextopia;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlParameterParser {

    final Logger logger = LoggerFactory.getLogger(UrlParameterParser.class);

    public Map<String, List<String>> parseQueryString(String queryString) {
        Map<String, List<String>> queryMap = new LinkedHashMap<>();
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
                String keyDecoded = URLDecoder.decode(key, "UTF-8");
                String valueDecoded = URLDecoder.decode(value, "UTF-8");
                if (!queryMap.containsKey(key)) {
                    queryMap.put(key, new ArrayList<String>());
                }
                queryMap.get(keyDecoded).add(valueDecoded);
            } catch (Exception e) {
                logger.error("Bad URL encoding", queryString);
            }
        }
        return queryMap;
    }
}