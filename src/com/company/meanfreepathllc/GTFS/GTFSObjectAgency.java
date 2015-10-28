package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.List;

/**
 * Created by nick on 10/27/15.
 */
public class GTFSObjectAgency extends GTFSObject {
    public final static String FIELD_AGENCY_ID = "agency_id",
    FIELD_AGENCY_NAME = "agency_name",
    FIELD_AGENCY_URL = "agency_url",
    FIELD_AGENCY_TIMEZONE = "agency_timezone",
    FIELD_AGENCY_LANG = "agency_lang",
    FIELD_AGENCY_PHONE = "agency_phone",
    FIELD_AGENCY_FARE_URL = "agency_fare_url";

    static {
        String[] df = {FIELD_AGENCY_ID, FIELD_AGENCY_NAME, FIELD_AGENCY_URL, FIELD_AGENCY_TIMEZONE, FIELD_AGENCY_LANG, FIELD_AGENCY_PHONE, FIELD_AGENCY_FARE_URL};
        definedFields = new String[df.length];
        short i = 0;
        for(String f : df) {
            definedFields[i++] = f;
        }
        String[] rf = {FIELD_AGENCY_NAME, FIELD_AGENCY_URL, FIELD_AGENCY_TIMEZONE};
        requiredFields = new String[rf.length];
        i = 0;
        for(String f : rf) {
            requiredFields[i++] = f;
        }
    }

    @Override
    public void postProcess() throws InvalidArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            String[] errMsg = {""};
            errMsg[0] = String.format("Missing the following fields: %s", String.join(", ", missingFields));
            throw new InvalidArgumentException(errMsg);
        }
    }
}
