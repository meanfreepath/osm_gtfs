package com.company.meanfreepathllc.GTFS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/27/15.
 */
public class GTFSObjectAgency extends GTFSObject {
    public final static int INITIAL_CAPACITY = 32;

    public final static String
            FIELD_AGENCY_ID = "agency_id",
            FIELD_AGENCY_NAME = "agency_name",
            FIELD_AGENCY_URL = "agency_url",
            FIELD_AGENCY_TIMEZONE = "agency_timezone",
            FIELD_AGENCY_LANG = "agency_lang",
            FIELD_AGENCY_PHONE = "agency_phone",
            FIELD_AGENCY_FARE_URL = "agency_fare_url";

    public final static String[] definedFields = {FIELD_AGENCY_ID, FIELD_AGENCY_NAME, FIELD_AGENCY_URL, FIELD_AGENCY_TIMEZONE, FIELD_AGENCY_LANG, FIELD_AGENCY_PHONE, FIELD_AGENCY_FARE_URL};
    public final static String[] requiredFields = {FIELD_AGENCY_NAME, FIELD_AGENCY_URL, FIELD_AGENCY_TIMEZONE};

    public GTFSObjectAgency() {
        fields = new HashMap<>(getDefinedFields().length);
    }

    @Override
    public void postProcess(GTFSDataset dataset) throws IllegalArgumentException {
        List<String> missingFields = checkRequiredFields();
        if (missingFields != null && missingFields.size() > 0) {
            throw new IllegalArgumentException(String.format("Missing the following fields: %s", String.join(", ", missingFields)));
        }

        //add to the main agency list
        dataset.addAgency(this);
    }
    @Override
    public String getFileName() {
        return "agency.txt";
    }
    @Override
    public String[] getDefinedFields() {
        return definedFields;
    }
    @Override
    public String[] getRequiredFields() {
        return requiredFields;
    }
}
