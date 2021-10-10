package com.searchspring.nextopia;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

public class UrlParameterParserTest {

    private UrlParameterParser parser = null;

    @Before
    public void setup() {
        parser = new UrlParameterParser();
    }

    @Test
    public void ParserTest() throws URISyntaxException {
        assertEquals("{keywords=[böb], page=[1]}", tryParams("keywords=böb&page=1"));
    }

    @Test
    public void ParserMultipleTest() throws URISyntaxException {
        assertEquals("{keywords=[böb, kebob], page=[1]}", tryParams("keywords=böb&keywords=kebob&page=1"));
        assertEquals("{keywords=[kebob, böb], page=[1]}", tryParams("keywords=kebob&keywords=böb&page=1"));
    }

    @Test
    public void EdgesTest() throws URISyntaxException {
        assertEquals("{}", tryParams(null));
        assertEquals("{}", tryParams(""));
        assertEquals("{}", tryParams("bob"));
        assertEquals("{}", tryParams("bob="));
        assertEquals("{}", tryParams("bob=&"));
        assertEquals("{}", tryParams("bob=&&&="));
        assertEquals("{bob=[==]}", tryParams("bob===&&&="));
        assertEquals("{}", tryParams("bob=&doug"));
        assertEquals("{}", tryParams("bob=&doug="));
        assertEquals("{doug=[mckenzie]}", tryParams("bob=&doug=mckenzie"));
        assertEquals("{}", tryParams("id=%%2342342"));
        assertEquals("{}", tryParams("%%2342342=value"));
    }
     
    private String tryParams(String params) {
       return parser.parseQueryString(params).toString();
    }
}