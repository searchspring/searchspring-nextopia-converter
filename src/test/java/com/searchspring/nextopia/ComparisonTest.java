package com.searchspring.nextopia;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ComparisonTest {
    private Converter converter = null;
    private String siteId = null;

    @Before
    public void setup() {
        if (siteId == null) {
            siteId = System.getenv("SITE_ID");
        }
        converter = new Converter(siteId);
    }

    @Test
    public void urlParsingTest() throws Exception {
        Assume.assumeNotNull(siteId);
        Assume.assumeTrue(new File("tmp/urls.txt").exists());
        String line = null;
        try (BufferedReader br = new BufferedReader(new FileReader("tmp/urls.txt"))) {
            while ((line = br.readLine()) != null) {
                converter.convertNextopiaQueryUrl(line);
            }
        } catch (Exception e) {
            System.out.println("Failed on URL: " + line);
            throw e;
        }

    }

    @Test
    public void xmlComparisonTest() throws Exception {
        Assume.assumeNotNull(siteId);
        Assume.assumeTrue(new File("tmp/nextopiaUrls.txt").exists());
        String line = null;
        try (BufferedReader br = new BufferedReader(new FileReader("tmp/nextopiaUrls.txt"))) {
            while ((line = br.readLine()) != null) {
                String nextopiaUrl = line;
                String searchspringUrl = converter.convertNextopiaQueryUrl(nextopiaUrl);
                compareXml(nextopiaUrl, searchspringUrl);
                break;
            }
        } catch (Exception e) {
            System.out.println("Failed on URL: " + line);
            throw e;
        }

    }

    private void compareXml(String nextopiaUrl, String searchspringUrl) throws Exception {
        String nextopiaXml = readStringFromURL(nextopiaUrl);
        String searchspringJson = readStringFromURL(searchspringUrl);
        System.out.println(nextopiaXml);
        System.out.println(searchspringJson);
        String searchspringXml = converter.convertSearchspringResponse(searchspringJson);
        System.out.println(searchspringXml);
        assertEquals(prettyPrint(nextopiaXml), prettyPrint(searchspringXml));
    }

    public static String readStringFromURL(String requestURL) throws Exception {
        System.out.println("executing: " + requestURL);
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    public String prettyPrint(String in) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(in));
        Document doc = db.parse(is);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        // initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        String xmlString = result.getWriter().toString();
        return xmlString;
    }
}
