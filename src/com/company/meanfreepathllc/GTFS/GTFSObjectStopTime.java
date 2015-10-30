package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/28/15.
 */
public class GTFSObjectStopTime extends GTFSObject {
    public final static int INITIAL_CAPACITY = 2097152;

    public final static String
            FIELD_TRIP_ID = "trip_id",
            FIELD_ARRIVAL_TIME = "arrival_time",
            FIELD_DEPARTURE_TIME = "departure_time",
            FIELD_STOP_ID = "stop_id",
            FIELD_STOP_SEQUENCE = "stop_sequence",
            FIELD_STOP_HEADSIGN = "stop_headsign",
            FIELD_PICKUP_TYPE = "pickup_type",
            FIELD_DROPOFF_TYPE = "drop_off_type",
            FIELD_SHAPE_DIST_TRAVELED = "shape_dist_traveled",
            FIELD_TIMEPOINT = "timepoint";

    public final static String[] definedFields = {FIELD_TRIP_ID, FIELD_ARRIVAL_TIME, FIELD_DEPARTURE_TIME, FIELD_STOP_ID, FIELD_STOP_SEQUENCE, FIELD_STOP_HEADSIGN, FIELD_PICKUP_TYPE, FIELD_DROPOFF_TYPE, FIELD_SHAPE_DIST_TRAVELED, FIELD_TIMEPOINT};
    public final static String[] requiredFields = {FIELD_TRIP_ID, FIELD_ARRIVAL_TIME, FIELD_DEPARTURE_TIME, FIELD_STOP_ID, FIELD_STOP_SEQUENCE};

    public final static List<GTFSObjectStopTime> allStopTimes = new ArrayList<>(INITIAL_CAPACITY);
   // public final static HashMap<String, GTFSObjectStopTime> stopTimeLookup = new HashMap<>(INITIAL_CAPACITY);

    public GTFSObjectTrip parentTrip;
    public GTFSObjectStop stop;

    public GTFSObjectStopTime() {
        fields = new HashMap<>(getDefinedFields().length);
    }
    @Override
    public void postProcess() throws InvalidArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            String[] errMsg = {""};
            errMsg[0] = String.format("Missing the following fields: %s", String.join(", ", missingFields));
            throw new InvalidArgumentException(errMsg);
        }

        //assign to the relevant trips and stops
        parentTrip = GTFSObjectTrip.tripLookup.get(getField(FIELD_TRIP_ID));
        stop = GTFSObjectStop.stopLookup.get(getField(FIELD_STOP_ID));

        parentTrip.addStopTime(this);

        addToList();
    }

    @Override
    protected void addToList() {
        allStopTimes.add(this);
       // stopTimeLookup.put(getField())
    }
    @Override
    public String getFileName() {
        return "stop_times.txt";
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
