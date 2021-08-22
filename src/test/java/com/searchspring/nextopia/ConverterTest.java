package com.searchspring.nextopia;

import static org.junit.Assert.assertEquals;

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

    @Before
    public void setup() {
        converter = new Converter();
    }

    @Test
    public void ConvertSearchspringResponsePaginationTest() {
        String ssJson = "{\"pagination\": {\"totalResults\": 1981}}";
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml><pagination> q<total_products>1981</total_products></pagination><results></results></xml>";
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
    public void ConvertNextopiaQueryTest() {
        assertEquals("newUrl", converter.convertNextopiaQueryUrl("http://blah"));
    }
}