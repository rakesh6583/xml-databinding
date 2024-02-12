package com.xmltoDB.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String createTableAndIngestData(MultipartFile file) throws IOException, ParserConfigurationException, SAXException {
        Map<String, String> columnNames = parseXMLAndExtractColumnNames(file);
        String tableName = createTableWithColumns(columnNames);
        ingestData(file, tableName, columnNames);
        return tableName;
    }

    private Map<String, String> parseXMLAndExtractColumnNames(MultipartFile file) throws IOException, ParserConfigurationException, SAXException {
        Map<String, String> columnNames = new LinkedHashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file.getInputStream());
        Element root = document.getDocumentElement();
        NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String columnName = element.getTagName();
                columnNames.put(columnName, "String");
            }
        }
        return columnNames;
    }

    private String createTableWithColumns(Map<String, String> columnNames) {
        String tableName = "generated_table";
        String columnsWithTypes = columnNames.entrySet().stream()
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining(", "));
        String createTableQuery = "CREATE TABLE " + tableName + " (" + columnsWithTypes + ")";
        jdbcTemplate.execute(createTableQuery);
        return tableName;
    }

    private void ingestData(MultipartFile file, String tableName, Map<String, String> columnNames) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file.getInputStream());
        Element root = document.getDocumentElement();
        NodeList authorNodes = root.getElementsByTagName("author");
        for (int i = 0; i < authorNodes.getLength(); i++) {
            Element authorElement = (Element) authorNodes.item(i);
            Map<String, String> data = extractDataFromAuthorElement(authorElement, columnNames);
            insertData(tableName, data);
        }
    }

    private Map<String, String> extractDataFromAuthorElement(Element authorElement, Map<String, String> columnNames) {
        Map<String, String> data = new LinkedHashMap<>();
        NodeList childNodes = authorElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String columnName = element.getTagName();
                String value = element.getTextContent();
                data.put(columnName, value);
            }
        }
        return data;
    }

    private void insertData(String tableName, Map<String, String> data) {
        String columnNames = String.join(", ", data.keySet());
        String placeholders = data.keySet().stream().map(key -> "?").collect(Collectors.joining(", "));
        String insertQuery = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";
        jdbcTemplate.update(insertQuery, data.values().toArray());
    }
}


