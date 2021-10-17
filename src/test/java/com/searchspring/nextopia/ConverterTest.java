package com.searchspring.nextopia;

import static org.junit.Assert.assertEquals;
import static com.searchspring.nextopia.model.ParameterMappings.*;
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
        private static final String EXPECTED_URL_PREFIX = "https://abcd12.a.searchspring.io/api?siteId=" + SITE_ID;

        @Before
        public void setup() {
                converter = new Converter(SITE_ID);
        }

        @Test
        public void ConvertSearchspringResponsePaginationTest() {
                String ssJson = "{\"pagination\": {\"totalResults\": 1981}}";
                String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><pagination><total_products>1981</total_products></pagination><results></results></xml>";
                assertEquals(expected, converter.convertSearchspringResponse(ssJson));
        }

        @Test
        public void ConvertSearchspringResponseResultsTest() {
                String ssJson = "{\"pagination\": {\"totalResults\": 1981}, \"results\": [ { \"brand\": \"Adidas\" } ]}";
                String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><pagination><total_products>1981</total_products></pagination>"
                                + "<results><result><rank>0</rank><brand>Adidas</brand></result></results>" //
                                + "</xml>";
                Source sourceExpected = Input.fromString(expected).build();
                Source sourceConverted = Input.fromString(converter.convertSearchspringResponse(ssJson)).build();
                Diff diff = DiffBuilder.compare(sourceExpected).withTest(sourceConverted).checkForSimilar()
                                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText,
                                                ElementSelectors.byName))
                                .build();
                Assert.assertFalse(diff.toString(), diff.hasDifferences());
        }

        @Test
        public void ConvertQueryKeywordTest() throws URISyntaxException {
                assertEquals(EXPECTED_URL_PREFIX + "&" + SS_KEYWORDS + "=b%C3%B6b", converter.convertNextopiaQueryUrl(
                                "https://ecommerce-search.nextopiasoftware.com/return-results.php?keywords=böb"));
        }

        @Test
        public void ConvertQueryRefineTest() throws URISyntaxException {
                assertEquals(EXPECTED_URL_PREFIX + "&" + SS_KEYWORDS + "=world+fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Dinner+Fork",
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&keywords=world+fork"
                                                + "&Flatwaretypeid7741124012283339335=Dinner+Fork"));
        }

        @Test
        public void ConvertQueryRefineOrTest() throws URISyntaxException {
                // TODO
                // https://api.nextopiasoftware.com/return-results.php?xml=1&client_id=083b5e2abbfa7278bb3c4821178e0d9b&keywords=Brush&refine=y&return_single_refines=1:1&ip=10.3.62.25&page=2&res_per_page=20&searchtype=0&Catalogidlist=3074457345616677067-PLG4.00^3074457345616676730-PLG4.00&requested_fields=Sku
        }

        @Test
        public void ConvertQueryRefineAndTest() throws URISyntaxException {
                assertEquals(EXPECTED_URL_PREFIX + "&" + SS_KEYWORDS + "=world+fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Spoon"
                                + "&filter.Flatwaretypeid7741124012283339335=Dinner+Fork",
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&keywords=world+fork"
                                                + "&Flatwaretypeid7741124012283339335=Spoon"
                                                + "&Flatwaretypeid7741124012283339335=Dinner+Fork"));
        }

        @Test
        public void ConvertQueryRefineAndEnsureOrderTest() throws URISyntaxException {
                assertEquals(EXPECTED_URL_PREFIX + "&" + SS_KEYWORDS + "=world+fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Dinner+Fork"
                                + "&filter.Flatwaretypeid7741124012283339335=Spoon",
                                converter.convertNextopiaQueryUrl(TEST_URL_PREFIX + "&keywords=world+fork"
                                                + "&Flatwaretypeid7741124012283339335=Dinner+Fork"
                                                + "&Flatwaretypeid7741124012283339335=Spoon"));
        }
}