package com.searchspring.nextopia;

import static com.searchspring.nextopia.model.ParameterMappings.SS_KEYWORDS;
import static com.searchspring.nextopia.model.ParameterMappings.SS_PAGE;
import static com.searchspring.nextopia.model.ParameterMappings.SS_RES_PER_PAGE;
import static com.searchspring.nextopia.model.ParameterMappings.*;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import javax.xml.transform.Source;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

public class ConverterTest {

        private Converter converter = null;
        private final static String SITE_ID = "abcd12";
        private static final String TEST_SEARCH_URL_PREFIX = "https://api.nextopiasoftware.com/return-results.php?xml=1&client_id=66141eeeacafe959b288238d65b176cb";
        private static final String TEST_AUTOCOMPLETE_URL_PREFIX = "https://vector.nextopiasoftware.com/return_autocomplete_jsonp_v3.php?callback=callback&cid=66141eeeacafe959b288238d65b176cb&_=000000000";
        private static final String EXPECTED_SEARCH_URL_PREFIX = "https://abcd12.a.searchspring.io/api/search/search.json?siteId="
                        + SITE_ID;
        private static final String EXPECTED_AUTOCOMPLETE_URL_PREFIX = "https://abcd12.a.searchspring.io/api/suggest/query?siteId="
                        + SITE_ID;
        private static final String PREFIX_SEARCH_EMPTY_BITS = "<query_time>0</query_time>";
        private static final String PREFIX_SEARCH_EMPTY_BITS2 = "<custom_synonyms/>";
        private static final String POSTFIX_SEARCH_EMPTY_BITS = "<searched_in_field/><user_search_depth/><currently_sorted_by/><sort_bys/><notices><related_added><![CDATA[ 0 ]]></related_added><sku_match><![CDATA[ 0 ]]></sku_match><or_switch><![CDATA[ 0 ]]></or_switch></notices><merchandizing/>";
        private static final String POSTFIX_SEARCH_EMPTY_BITS2 = "<index_hash><![CDATA[ aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa ]]></index_hash><xml_feed_done>1</xml_feed_done><cl>0</cl>";

        @Before
        public void setup() {
                converter = new Converter(SITE_ID);
        }

        @Test
        public void convertAutocompleteUrlTest() throws URISyntaxException {
                String url = converter.convertNextopiaAutocompleteQueryUrl(TEST_AUTOCOMPLETE_URL_PREFIX + "&q=red");
                assertEquals(EXPECTED_AUTOCOMPLETE_URL_PREFIX + "&" + SS_AUTOCOMPLETE_QUERY + "=red", url);
        }

        @Test
        public void emptyTest() throws Exception {
                String expected = "<?xml version='1.0' encoding='UTF-8'?><xml><query_time>0</query_time><suggested_spelling><![CDATA[ ]]></suggested_spelling><custom_synonyms/><pagination>"
                                + "<total_products>0</total_products></pagination><searched_in_field/><user_search_depth/><currently_sorted_by/><sort_bys/><notices><related_added><![CDATA[ 0 ]]></related_added>"
                                + "<sku_match><![CDATA[ 0 ]]></sku_match><or_switch><![CDATA[ 0 ]]></or_switch></notices><merchandizing/><refinables/><results/>"
                                + "<index_hash><![CDATA[ aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa ]]></index_hash><xml_feed_done>1</xml_feed_done><cl>0</cl></xml>";
                assertEquals(expected, converter.convertSearchspringSearchResponse(""));
                assertEquals(expected, converter.convertSearchspringSearchResponse("{}"));
                assertEquals(expected, converter.convertSearchspringSearchResponse(null));
        }

        @Test
        public void convertSearchspringResponsePaginationTest() {
                String ssJson = "{\"pagination\": {\"totalResults\": 1981}}";
                String expected = "<?xml version='1.0' encoding='UTF-8'?><xml>"
                                + PREFIX_SEARCH_EMPTY_BITS
                                + "<suggested_spelling><![CDATA[ ]]></suggested_spelling>"
                                + PREFIX_SEARCH_EMPTY_BITS2
                                + "<pagination><total_products>1981</total_products><product_min>0</product_min><product_max>0</product_max><current_page>0</current_page><total_pages>0</total_pages><prev_page>0</prev_page><next_page>0</next_page></pagination>"
                                + POSTFIX_SEARCH_EMPTY_BITS
                                + "<refinables/><results/>"
                                + POSTFIX_SEARCH_EMPTY_BITS2
                                + "</xml>";
                assertEquals(expected, converter.convertSearchspringSearchResponse(ssJson));
        }

        @Test
        public void convertSearchspringResponseResultsTest() {
                String ssJson = "{\"pagination\": {\"totalResults\": 1981},"
                                + "\"didYouMean\": {\"query\": \"span\",\"highlighted\": \"\\u003cem\\u003espan\\u003c/em\\u003e\"},"
                                + "\"results\": [ { \"uid\":\"1234\",\"brand\": \"Adidas\" } ],"
                                + "\"facets\": [{\"field\":\"pattern_id7741124012283333869\", \"label\": \"Pattern\",\"type\": null,\"collapse\": 0,\"facet_active\": 0,"
                                + "\"values\": [{\"active\": false,\"type\": \"value\",\"value\": \"Baguette\",\"label\": \"Baguette\",\"count\": 21}]"
                                + "}]" + "}";
                String expected = "<?xml version='1.0' encoding='UTF-8'?><xml>"
                                + PREFIX_SEARCH_EMPTY_BITS
                                + "<suggested_spelling><![CDATA[span]]></suggested_spelling>"
                                + PREFIX_SEARCH_EMPTY_BITS2
                                + "<pagination><total_products>1981</total_products><product_min>0</product_min><product_max>0</product_max><current_page>0</current_page><total_pages>0</total_pages><prev_page>0</prev_page><next_page>0</next_page></pagination>"
                                + POSTFIX_SEARCH_EMPTY_BITS
                                + "<refinables><refinable><name><![CDATA[pattern_id7741124012283333869]]></name><values><value><name><![CDATA[Baguette]]></name><num><![CDATA[21]]></num></value></values></refinable></refinables>"
                                + "<results>" //
                                + "<result><rank>0</rank><Sku><![CDATA[1234]]></Sku><results_flags><![CDATA[attributized]]></results_flags></result>" //
                                + "</results>" //
                                + POSTFIX_SEARCH_EMPTY_BITS2
                                + "</xml>";
                String converted = converter.convertSearchspringSearchResponse(ssJson);
                Source sourceExpected = Input.fromString(expected).build();
                Source sourceConverted = Input.fromString(converted).build();
                Diff diff = DiffBuilder.compare(sourceExpected).withTest(sourceConverted).checkForSimilar()
                                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText,
                                                ElementSelectors.byName))
                                .build();
                Assert.assertFalse(diff.toString(), diff.hasDifferences());
        }

        @Test
        public void convertFailingTest() throws Exception {
                String line = null;
                InputStream is = this.getClass().getResourceAsStream("/tests.json");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                        while ((line = br.readLine()) != null) {
                                converter.convertSearchspringSearchResponse(line);
                        }
                } catch (Exception e) {
                        System.out.println("Failed on json: " + line);
                        throw e;
                }
        }

        @Test
        public void convertQueryKeywordTest() throws URISyntaxException {
                assertEquals(EXPECTED_SEARCH_URL_PREFIX + "&" + SS_KEYWORDS + "=b%C3%B6b",
                                converter.convertNextopiaSearchQueryUrl(
                                                "https://ecommerce-search.nextopiasoftware.com/return-results.php?keywords=böb"));
        }

        @Test
        public void convertQueryRefineTest() throws URISyntaxException {
                assertEquals(EXPECTED_SEARCH_URL_PREFIX + "&" + SS_KEYWORDS + "=world+fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Dinner+Fork",
                                converter.convertNextopiaSearchQueryUrl(TEST_SEARCH_URL_PREFIX + "&keywords=world+fork"
                                                + "&Flatwaretypeid7741124012283339335=Dinner+Fork"));
        }

        @Test
        public void convertQueryRefineOrTest() throws URISyntaxException {
                assertEquals(EXPECTED_SEARCH_URL_PREFIX + "&" + SS_KEYWORDS + "=Brush"
                                + "&filter.Catalogidlist=3074457345616677067-PLG4.00"
                                + "&filter.Catalogidlist=3074457345616676730-PLG4.00",
                                converter.convertNextopiaSearchQueryUrl(TEST_SEARCH_URL_PREFIX + "&keywords=Brush"
                                                + "&Catalogidlist=3074457345616677067-PLG4.00^3074457345616676730-PLG4.00"));
        }

        @Test
        public void convertQueryRefineAndTest() throws URISyntaxException {
                assertEquals(EXPECTED_SEARCH_URL_PREFIX + "&" + SS_KEYWORDS + "=world+fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Spoon"
                                + "&filter.Flatwaretypeid7741124012283339335=Dinner+Fork",
                                converter.convertNextopiaSearchQueryUrl(TEST_SEARCH_URL_PREFIX + "&keywords=world+fork"
                                                + "&Flatwaretypeid7741124012283339335=Spoon"
                                                + "&Flatwaretypeid7741124012283339335=Dinner+Fork"));
        }

        @Test
        public void convertQueryRefineAndEnsureOrderTest() throws URISyntaxException {
                assertEquals(EXPECTED_SEARCH_URL_PREFIX + "&" + SS_KEYWORDS + "=world+fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Dinner+Fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Spoon",
                                converter.convertNextopiaSearchQueryUrl(TEST_SEARCH_URL_PREFIX + "&keywords=world+fork"
                                                + "&Flatwaretypeid7741124012283339335=Dinner+Fork"
                                                + "&Flatwaretypeid7741124012283339335=Spoon"));
        }

        @Test
        public void paginationTest() throws URISyntaxException {
                assertEquals(EXPECTED_SEARCH_URL_PREFIX + "&" + SS_PAGE + "=2" + "&" + SS_RES_PER_PAGE + "=32",
                                converter.convertNextopiaSearchQueryUrl(
                                                TEST_SEARCH_URL_PREFIX + "&page=2" + "&res_per_page=32"));
        }

        @Test
        public void sortTest() throws URISyntaxException {
                assertEquals(EXPECTED_SEARCH_URL_PREFIX + "&sort.Price=asc",
                                converter.convertNextopiaSearchQueryUrl(
                                                TEST_SEARCH_URL_PREFIX + "&sort_by_field=Price:ASC"));
                assertEquals(EXPECTED_SEARCH_URL_PREFIX + "&sort.Price=desc",
                                converter.convertNextopiaSearchQueryUrl(
                                                TEST_SEARCH_URL_PREFIX + "&sort_by_field=Price:DESC"));
                assertEquals(EXPECTED_SEARCH_URL_PREFIX,
                                converter.convertNextopiaSearchQueryUrl(
                                                TEST_SEARCH_URL_PREFIX + "&sort_by_field=Price:"));
                assertEquals(EXPECTED_SEARCH_URL_PREFIX,
                                converter.convertNextopiaSearchQueryUrl(
                                                TEST_SEARCH_URL_PREFIX + "&sort_by_field=Price"));
        }
}