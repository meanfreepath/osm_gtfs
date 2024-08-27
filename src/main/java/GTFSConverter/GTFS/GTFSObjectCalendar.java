package GTFSConverter.GTFS;

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
    public final static String GTFS_FILE_NAME = "calendar.txt";

    public GTFSObjectCalendar() {
        fields = new HashMap<>(getDefinedFields().length);
    }
    @Override
    public void postProcess(GTFSDataset dataset) throws IllegalArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            throw new IllegalArgumentException(String.format("Missing the following fields: %s", String.join(", ", missingFields)));
        }

        //process any other fields


        //add to the main calendar list
        dataset.allCalendars.add(this);
        dataset.calendarLookup.put(getField(FIELD_SERVICE_ID), this);
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
