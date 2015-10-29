package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/28/15.
 */
public class GTFSObjectCalendar extends GTFSObject {
    public final static int INITIAL_CAPACITY = 128;

    public final static String
            FIELD_SERVICE_ID = "service_id",
            FIELD_MONDAY = "monday",
            FIELD_TUESDAY = "tuesday",
            FIELD_WEDNESDAY = "wednesday",
            FIELD_THURSDAY = "thursday",
            FIELD_FRIDAY = "friday",
            FIELD_SATURDAY = "saturday",
            FIELD_SUNDAY = "sunday",
            FIELD_START_DATE = "start_date",
            FIELD_END_DATE = "end_date";

    public final static String[] definedFields = {FIELD_SERVICE_ID, FIELD_MONDAY, FIELD_TUESDAY, FIELD_WEDNESDAY, FIELD_THURSDAY, FIELD_FRIDAY, FIELD_SATURDAY, FIELD_SUNDAY, FIELD_START_DATE, FIELD_END_DATE};
    public final static String[] requiredFields = {FIELD_SERVICE_ID, FIELD_MONDAY, FIELD_TUESDAY, FIELD_WEDNESDAY, FIELD_THURSDAY, FIELD_FRIDAY, FIELD_SATURDAY, FIELD_SUNDAY};

    public final static List<GTFSObjectCalendar> allCalendars = new ArrayList<>(INITIAL_CAPACITY);
    public final static HashMap<String, GTFSObjectCalendar> calendarLookup = new HashMap<>(INITIAL_CAPACITY);

    @Override
    public void postProcess() throws InvalidArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            String[] errMsg = {""};
            errMsg[0] = String.format("Missing the following fields: %s", String.join(", ", missingFields));
            throw new InvalidArgumentException(errMsg);
        }

        //process any other fields


        //add to the main calendar list
        addToList();
    }

    @Override
    public String[] getDefinedFields() {
        return definedFields;
    }

    @Override
    public String[] getRequiredFields() {
        return requiredFields;
    }

    @Override
    protected void addToList() {
        allCalendars.add(this);
        calendarLookup.put(getField(FIELD_SERVICE_ID), this);
    }
}
