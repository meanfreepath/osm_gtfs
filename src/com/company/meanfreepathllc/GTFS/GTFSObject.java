package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/27/15.
 */
public abstract class GTFSObject {
    private final static HashMap<Short, String> gtfsColumnIndices = new HashMap<>(32);
    private static int internalIdValue = 0;

    protected HashMap<String,String> fields;
    public final int internalId;

    /**
     * Other, nonstandard fields that are in the GTFS dataset
     */
    protected HashMap<String,String> nonstandardFields;

    public GTFSObject() {
        internalId = --internalIdValue;
    }
    public String getField(String name) {
        return fields.get(name);
    }
    public void setField(String fieldName, String value) {
        fields.put(fieldName, value.trim());
    }
    public String getNonStandardField(String name) {
        return nonstandardFields.get(name);
    }
    public void setNonStandardField(String fieldName, String value) {
        if(nonstandardFields == null) {
            nonstandardFields = new HashMap<>();
        }
        nonstandardFields.put(fieldName, value.trim());
    }

    public abstract void postProcess() throws InvalidArgumentException;
    public abstract String getFileName();
    public abstract String[] getDefinedFields();
    public abstract String[] getRequiredFields();
    protected abstract void addToList();

    protected final List<String> checkRequiredFields() {
        List<String> missingFields = null;
        for(String f: getRequiredFields()) {
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
