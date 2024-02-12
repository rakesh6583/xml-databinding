package com.xmltoDB.Model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class DataWrapper {
    private String tableName;
    private byte[] data;

    public DataWrapper(String tableName, byte[] data) {
        this.tableName = tableName;
        this.data = data;
    }

    // Getters and setters
}
