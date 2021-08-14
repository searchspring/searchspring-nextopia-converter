package com.searchspring.nextopia;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConverterTest {
    @Test
    public void ConvertSearchspringResponseTest() {
        Converter converter = new Converter();
        assertEquals("<xml>", converter.convertSearchspringResponse("{}"));
    }
    @Test
    public void ConvertNextopiaQueryTest() {
        Converter converter = new Converter();
        assertEquals("newUrl", converter.convertNextopiaQueryUrl("http://blah"));
    }
}