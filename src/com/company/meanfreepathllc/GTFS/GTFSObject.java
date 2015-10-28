package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/27/15.
 */
public abstract class GTFSObject {


    public static String[] definedFields;
    public static String[] requiredFields;

    protected final HashMap<String,String> fields;

    public GTFSObject() {
        fields = new HashMap<>(definedFields.length);
    }

    public String getField(String name) {
        return fields.get(name);
    }
    public void setField(String fieldName, String value) {
        fields.put(fieldName, value);
    }
    public abstract void postProcess() throws InvalidArgumentException;

    protected List<String> checkRequiredFields() {
        List<String> missingFields = null;
        for(String f: requiredFields) {
            if(!fields.containsKey(f)) {
                if(missingFields == null) {
                    missingFields = new ArrayList<>();
                }
                missingFields.add(f);
            }
        }
        return missingFields;
    }
}
