package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/27/15.
 */
public class GTFSObjectAgency extends GTFSObject {
    public enum AgencyIdStatus {
        Unknown, Present, NotPresent
    }

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
    public static AgencyIdStatus agencyIdStatus = AgencyIdStatus.Unknown;

    public final static List<GTFSObjectAgency> allAgencies = new ArrayList<>(INITIAL_CAPACITY);
    private final static HashMap<String, GTFSObjectAgency> agencyLookup = new HashMap<>(INITIAL_CAPACITY);

    public GTFSObjectAgency() {
        fields = new HashMap<>(getDefinedFields().length);
    }

    @Override
    protected void addToList() {
        allAgencies.add(this);

        //flag whether the agency ID is present
        String agencyId = getField(FIELD_AGENCY_ID);
        if(agencyIdStatus == AgencyIdStatus.Unknown) {
            agencyIdStatus = agencyId != null ? AgencyIdStatus.Present : AgencyIdStatus.NotPresent;
        }

        //only add to the lookup if IDs are present
        if(agencyIdStatus == AgencyIdStatus.Present) {
            agencyLookup.put(agencyId, this);
        }
    }

    @Override
    public void postProcess() throws InvalidArgumentException {
        List<String> missingFields = checkRequiredFields();
        if (missingFields != null && missingFields.size() > 0) {
            String[] errMsg = {""};
            errMsg[0] = String.format("Missing the following fields: %s", String.join(", ", missingFields));
            throw new InvalidArgumentException(errMsg);
        }

        //add to the main agency list
        addToList();
    }

    public static GTFSObjectAgency lookupAgencyById(String id) {
        switch (agencyIdStatus) {
            case Unknown:
                return null;
            case Present:
                return agencyLookup.get(id);
            case NotPresent:
                if(allAgencies.size() > 0) {
                    return allAgencies.get(0);
                } else {
                    return null;
                }
        }
        return null;
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
