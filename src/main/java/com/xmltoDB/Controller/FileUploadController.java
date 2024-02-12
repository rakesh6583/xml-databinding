package com.xmltoDB.Controller;

import com.xmltoDB.Service.DatabaseService;
import com.xmltoDB.Service.XMLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@RestController
public class FileUploadController {
    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private XMLService xmlService;

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        // Validate file extension and size
        if (!file.getOriginalFilename().toLowerCase().endsWith(".xml")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only XML files are allowed.");
        }
        if (file.getSize() > 100 * 1024 * 1024) { // 100 MB limit
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size exceeds the limit.");
        }

        try {
            // Parse XML, execute DDL, ingest data
            String tableName = databaseService.createTableAndIngestData(file);
            // Send acknowledgment
            return ResponseEntity.ok("Successful Ingestion. Table created: " + tableName);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the file.");
        }
    }

    @PostMapping("/generate-xml")
    public ResponseEntity<byte[]> generateXML(@RequestParam("selectedTable") String selectedTable) {
        try {
            // Fetch data from selected table, limit to 100 rows
            byte[] xmlData = xmlService.generateXML(selectedTable);
            // Provide XML file for download
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + selectedTable + ".xml")
                    .body(xmlData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
