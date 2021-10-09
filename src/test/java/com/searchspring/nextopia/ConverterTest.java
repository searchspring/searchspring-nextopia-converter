package com.searchspring.nextopia;

import static org.junit.Assert.assertEquals;

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
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText, ElementSelectors.byName))
                .build();
        Assert.assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void ConvertQueryKeywordTest() throws URISyntaxException {
        assertEquals("https://abcd12.a.searchspring.io/api?siteId=" + SITE_ID + "&query=b%C3%B6b",
                converter.convertNextopiaQueryUrl(
                        "https://ecommerce-search.nextopiasoftware.com/return-results.php?keywords=böb&page=1&json=1&client_id=b9d7aa2736f88c2a410dde6f66b946b5&ip=50.92.124.92&user_agent=Mozilla%2F5.0+%28Macintosh%3B+Intel+Mac+OS+X+10_15_7%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F93.0.4577.82+Safari%2F537.36"));
    }

    @Test
    public void ConvertQueryRefineTest() throws URISyntaxException {
        //                                                          siteId=c2sajm&filter.panini_press_type_id7741124012283341841=Smooth%20Plates%7C_%7C7741124012284472342&experimentalMode=discrete-strict&debug=1&resultsPerPage=36&backend=saluki&preview=1
        assertEquals("https://abcd12.a.searchspring.io/api?siteId=" + SITE_ID + "&query=world+fork&filter.Flatwaretypeid7741124012283339335=Dinner+Fork",
                converter.convertNextopiaQueryUrl(
                        "https://api.nextopiasoftware.com/return-results.php?xml=1&client_id=66141eeeacafe959b288238d65b176cb&keywords=world+fork&refine=y&return_single_refines=1:1&Flatwaretypeid7741124012283339335=Dinner+Fork&refines_mode=keep:Flatwaretypeid7741124012283339335&ip=10.3.62.24&page=1&res_per_page=20&searchtype=0&requested_fields=Sku"));
    }
    @Test
    public void ConvertQueryRefineOrTest() throws URISyntaxException {
       // TODO 
    }
    @Test
    public void ConvertQueryRefineAndTest() throws URISyntaxException {
       // TODO 
    }
}