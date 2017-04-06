package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/28/16.
 */
public class GTFSObjectCalendarDate extends GTFSObject {
    public final static String
            FIELD_SERVICE_ID = "service_id",
            FIELD_DATE = "date",
            FIELD_EXCEPTION_TYPE = "exception_type";

    public final static String[] definedFields = {FIELD_SERVICE_ID, FIELD_DATE, FIELD_EXCEPTION_TYPE};
    public final static String[] requiredFields = {FIELD_SERVICE_ID, FIELD_DATE, FIELD_EXCEPTION_TYPE};
    public final static String GTFS_FILE_NAME = "calendar_dates.txt";
    private final static SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyyMMdd");

    public Date date;
    public short exceptionType;

    public GTFSObjectCalendarDate() {
        fields = new HashMap<>(getDefinedFields().length);
    }

    @Override
    public void postProcess(GTFSDataset dataset) throws InvalidArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            String[] errMsg = {""};
            errMsg[0] = String.format("Missing the following fields: %s", String.join(", ", missingFields));
            throw new InvalidArgumentException(errMsg);
        }
        try {
            date = DATE_PARSER.parse(getField(FIELD_DATE));
        } catch (ParseException e) {
            date = null;
            e.printStackTrace();
        }
        exceptionType = Short.valueOf(getField(FIELD_EXCEPTION_TYPE));


        //process any other fields


        //add to the main calendar list
        addToList(dataset);
    }
    private void addToList(GTFSDataset dataset) {
        final String serviceId = getField(FIELD_SERVICE_ID), serviceDate = getField(FIELD_DATE);
        GTFSObjectCalendar calendar = dataset.calendarLookup.get(serviceId);
        if(calendar == null) {
            //initialize the GTFS calendar object with default values
            calendar = new GTFSObjectCalendar();
            calendar.setField(GTFSObjectCalendar.FIELD_SERVICE_ID, serviceId);
            calendar.setField(GTFSObjectCalendar.FIELD_START_DATE, serviceDate);
            calendar.setField(GTFSObjectCalendar.FIELD_END_DATE, serviceDate);
            calendar.setField(GTFSObjectCalendar.FIELD_MONDAY, "0");
            calendar.setField(GTFSObjectCalendar.FIELD_TUESDAY, "0");
            calendar.setField(GTFSObjectCalendar.FIELD_WEDNESDAY, "0");
            calendar.setField(GTFSObjectCalendar.FIELD_THURSDAY, "0");
            calendar.setField(GTFSObjectCalendar.FIELD_FRIDAY, "0");
            calendar.setField(GTFSObjectCalendar.FIELD_SATURDAY, "0");
            calendar.setField(GTFSObjectCalendar.FIELD_SUNDAY, "0");

            dataset.allCalendars.add(calendar);
            dataset.calendarLookup.put(serviceId, calendar);
        } else {
            if(calendar.getField(GTFSObjectCalendar.FIELD_START_DATE).compareTo(serviceDate) > 0) {
                calendar.setField(GTFSObjectCalendar.FIELD_START_DATE, serviceDate);
            }
            if(calendar.getField(GTFSObjectCalendar.FIELD_END_DATE).compareTo(serviceDate) < 0) {
                calendar.setField(GTFSObjectCalendar.FIELD_END_DATE, serviceDate);
            }
        }

        if(getField(FIELD_EXCEPTION_TYPE).equals("1")) {
            final Calendar c = Calendar.getInstance();
            c.setTime(date);
            final int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            switch (dayOfWeek) {
                case Calendar.SUNDAY:
                    calendar.setField(GTFSObjectCalendar.FIELD_SUNDAY, "1");
                    break;
                case Calendar.MONDAY:
                    calendar.setField(GTFSObjectCalendar.FIELD_MONDAY, "1");
                    break;
                case Calendar.TUESDAY:
                    calendar.setField(GTFSObjectCalendar.FIELD_TUESDAY, "1");
                    break;
                case Calendar.WEDNESDAY:
                    calendar.setField(GTFSObjectCalendar.FIELD_WEDNESDAY, "1");
                    break;
                case Calendar.THURSDAY:
                    calendar.setField(GTFSObjectCalendar.FIELD_THURSDAY, "1");
                    break;
                case Calendar.FRIDAY:
                    calendar.setField(GTFSObjectCalendar.FIELD_FRIDAY, "1");
                    break;
                case Calendar.SATURDAY:
                    calendar.setField(GTFSObjectCalendar.FIELD_SATURDAY, "1");
                    break;
            }
        }
    }
    @Override
    public String getFileName() {
        return GTFS_FILE_NAME;
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
