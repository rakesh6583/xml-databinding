package com.xmltoDB.Service;

import com.xmltoDB.Model.DataWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class XMLService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public byte[] generateXML(String tableName) throws Exception {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + tableName + " LIMIT 100");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElement = document.createElement(tableName);
        document.appendChild(rootElement);
        for (Map<String, Object> row : rows) {
            Element rowElement = document.createElement("row");
            rootElement.appendChild(rowElement);
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                Element columnElement = document.createElement(entry.getKey());
                columnElement.appendChild(document.createTextNode(entry.getValue().toString()));
                rowElement.appendChild(columnElement);
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(outputStream));
        return outputStream.toByteArray();
    }
}