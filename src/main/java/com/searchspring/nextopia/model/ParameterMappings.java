package com.searchspring.nextopia.model;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ParameterMappings {
    public final static String SS_KEYWORDS = "q";
    public final static String SS_SITE_ID = "siteId";

    public final static String NX_KEYWORDS = "keywords";
    public final static String NX_CLIENT_ID = "client_id";
    public final static String NX_IP = "ip";
    public final static String NX_PAGE = "page";
    public final static String NX_REFINE = "refine";
    public final static String NX_REFINES_MODE = "refines_mode";
    public final static String NX_REQUESTED_FIELDS = "requested_fields";
    public final static String NX_RECS_PER_PAGE = "res_per_page";
    public final static String NX_RETURN_SINGLE_REFINES = "return_single_refines";
    public final static String NX_SEARCHTYPE = "searchtype";
    public final static String NX_XML = "xml";
    public final static String NX_USER_AGENT = "user_agent";

    public final static String NX_ABSTRACTED_FIELDS = "abstracted_fields";
    public final static String NX_FORCE_OR_SEARCH = "force_or_search";
    public final static String NX_INITIAL_SORT = "initial_sort";
    public final static String NX_INITIAL_SORT_ORDER = "initial_sort_order";
    public final static String NX_NO_METAPHONES = "no_metaphones";
    public final static String NX_NO_STEMMING = "no_stemming";
    public final static String NX_SORT_BY_FIELD = "sort_by_field";
    public final static String NX_TRIM_LENGTH = "trim_length";
    public final static String NX_TRIMMED_FIELDS = "trimmed_fields";

    public final static String NX_AND_REFINES = "and_refines";
    public final static String NX_COMPACT_REFINES = "compact_refines";
    public final static String NX_CUSTOM_RANGES = "custom_ranges";
    public final static String NX_PAGINATE_REFINES = "paginate_refines";
    public final static String NX_REQUESTED_REFINES = "requested_refines";


    public final static String NX_DISABLE_MERCHANDIZING = "disable_merchandizing";
    public final static String NX_IGNORE_REDIRECTS = "ignore_redirects";
        
    public final static String NX_CACHE_CALL = "cache_call";
    public final static String NX_MIN_MAX_VALUES = "min_max_values";
    public final static String NX_RELATED_SEARCHES = "related_searches";
    public final static String NX_JSON = "json";
        
    public final static String NX_LANDING_PAGE = "nxt_landing_page";

    public final static Set<String> ALL_NEXTOPIA_PARAMETERS = new HashSet<>();

    static {
        try {
            Field[] fields = ParameterMappings.class.getFields();
            for (Field field : fields) {
                if (field.getType() == String.class && field.getName().startsWith("NX_")) {
                    ALL_NEXTOPIA_PARAMETERS.add(field.get(ParameterMappings.class).toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
