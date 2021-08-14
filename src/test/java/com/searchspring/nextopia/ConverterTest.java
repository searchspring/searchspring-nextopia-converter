package com.searchspring.nextopia;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConverterTest {
    @Test
    public void ConvertTest() {
        Converter converter = new Converter();

        assertEquals("<xml>", converter.convert("{}"));
    }
}