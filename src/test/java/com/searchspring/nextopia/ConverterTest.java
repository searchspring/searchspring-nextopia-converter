package com.searchspring.nextopia;

import static com.searchspring.nextopia.model.ParameterMappings.SS_KEYWORDS;
import static com.searchspring.nextopia.model.ParameterMappings.SS_PAGE;
import static com.searchspring.nextopia.model.ParameterMappings.SS_RES_PER_PAGE;
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

        private static final String TEST_URL_PREFIX = "https://api.nextopiasoftware.com/return-results.php?xml=1&client_id=66141eeeacafe959b288238d65b176cb";
        private Converter converter = null;
        private final static String SITE_ID = "abcd12";
        private static final String EXPECTED_URL_PREFIX = "https://abcd12.a.searchspring.io/api/search/search.json?siteId="
                        + SITE_ID + "&resultsFormat=json";

        @Before
        public void setup() {
                converter = new Converter(SITE_ID);
        }

        @Test
        public void convertSearchspringResponsePaginationTest() {
                String ssJson = "{\"pagination\": {\"totalResults\": 1981}}";
                String expected = "<?xml version='1.0' encoding='UTF-8'?><xml>"
                                + "<pagination><total_products>1981</total_products><product_min>0</product_min><product_max>0</product_max><current_page>0</current_page><total_pages>0</total_pages><prev_page>0</prev_page><next_page>0</next_page></pagination>"
                                + "<refinables></refinables><results></results></xml>";
                assertEquals(expected, converter.convertSearchspringResponse(ssJson));
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
                                + "<suggested_spelling><![CDATA[span]]></suggested_spelling>"
                                + "<pagination><total_products>1981</total_products><product_min>0</product_min><product_max>0</product_max><current_page>0</current_page><total_pages>0</total_pages><prev_page>0</prev_page><next_page>0</next_page></pagination>"
                                + "<refinables><refinable><name><![CDATA[pattern_id7741124012283333869]]></name><values><value><name><![CDATA[Baguette]]></name><num><![CDATA[21]]></num></value></values></refinable></refinables>"
                                + "<results>" //
                                + "<result><rank>0</rank><Sku><![CDATA[1234]]></Sku><results_flags><![CDATA[attributized]]></results_flags></result>" //
                                + "</results>" //
                                + "</xml>";
                String converted = converter.convertSearchspringResponse(ssJson);
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
                                converter.convertSearchspringResponse(line);
                        }
                } catch (Exception e) {
                        System.out.println("Failed on json: " + line);
                        throw e;
                }
        }

        @Test
        public void convertQueryKeywordTest() throws URISyntaxException {
                assertEquals(EXPECTED_URL_PREFIX + "&" + SS_KEYWORDS + "=b%C3%B6b", converter.convertNextopiaQueryUrl(
                                "https://ecommerce-search.nextopiasoftware.com/return-results.php?keywords=böb"));
        }

        @Test
        public void convertQueryRefineTest() throws URISyntaxException {
                assertEquals(EXPECTED_URL_PREFIX + "&" + SS_KEYWORDS + "=world+fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Dinner+Fork",
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&keywords=world+fork"
                                                + "&Flatwaretypeid7741124012283339335=Dinner+Fork"));
        }

        @Test
        public void convertQueryRefineOrTest() throws URISyntaxException {
                assertEquals(EXPECTED_URL_PREFIX + "&" + SS_KEYWORDS + "=Brush"
                                + "&filter.Catalogidlist=3074457345616677067-PLG4.00"
                                + "&filter.Catalogidlist=3074457345616676730-PLG4.00",
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&keywords=Brush"
                                                + "&Catalogidlist=3074457345616677067-PLG4.00^3074457345616676730-PLG4.00"));
        }

        @Test
        public void convertQueryRefineAndTest() throws URISyntaxException {
                assertEquals(EXPECTED_URL_PREFIX + "&" + SS_KEYWORDS + "=world+fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Spoon"
                                + "&filter.Flatwaretypeid7741124012283339335=Dinner+Fork",
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&keywords=world+fork"
                                                + "&Flatwaretypeid7741124012283339335=Spoon"
                                                + "&Flatwaretypeid7741124012283339335=Dinner+Fork"));
        }

        @Test
        public void convertQueryRefineAndEnsureOrderTest() throws URISyntaxException {
                assertEquals(EXPECTED_URL_PREFIX + "&" + SS_KEYWORDS + "=world+fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Dinner+Fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Spoon",
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&keywords=world+fork"
                                                + "&Flatwaretypeid7741124012283339335=Dinner+Fork"
                                                + "&Flatwaretypeid7741124012283339335=Spoon"));
        }

        @Test
        public void paginationTest() throws URISyntaxException {
                assertEquals(EXPECTED_URL_PREFIX + "&" + SS_PAGE + "=2" + "&" + SS_RES_PER_PAGE + "=32",
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&page=2" + "&res_per_page=32"));
        }

        @Test
        public void sortTest() throws URISyntaxException {
                assertEquals(EXPECTED_URL_PREFIX + "&sort.Price=asc",
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&sort_by_field=Price:ASC"));
                assertEquals(EXPECTED_URL_PREFIX + "&sort.Price=desc",
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&sort_by_field=Price:DESC"));
                assertEquals(EXPECTED_URL_PREFIX,
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&sort_by_field=Price:"));
                assertEquals(EXPECTED_URL_PREFIX,
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&sort_by_field=Price"));
        }
}