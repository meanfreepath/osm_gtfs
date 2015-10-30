package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.*;

/**
 * Created by nick on 10/28/15.
 */
public class GTFSObjectTrip extends GTFSObject {
    public final static int INITIAL_CAPACITY = 65536, INITIAL_CAPACITY_STOPS = 128;
    private final static String DIRECTION_ID_0 = "0", DIRECTION_ID_1 = "1";

    public enum GTFSTripDirection {
        direction0, direction1
    }

    public final static String FIELD_ROUTE_ID = "route_id",
            FIELD_TRIP_ID = "trip_id",
            FIELD_SERVICE_ID = "service_id",
            FIELD_TRIP_SHORT_NAME = "trip_short_name",
            FIELD_TRIP_HEADSIGN = "trip_headsign",
            FIELD_DIRECTION_ID = "direction_id",
            FIELD_BLOCK_ID = "block_id",
            FIELD_SHAPE_ID = "shape_id",
            FIELD_WHEELCHAIR_ACCESSIBLE = "wheelchair_accessible",
            FIELD_BIKES_ALLOWED = "bikes_allowed";

    public final static String[] definedFields = {FIELD_ROUTE_ID, FIELD_SERVICE_ID, FIELD_TRIP_ID, FIELD_TRIP_SHORT_NAME, FIELD_TRIP_HEADSIGN, FIELD_DIRECTION_ID, FIELD_BLOCK_ID, FIELD_SHAPE_ID, FIELD_WHEELCHAIR_ACCESSIBLE, FIELD_BIKES_ALLOWED};
    public final static String[] requiredFields = {FIELD_ROUTE_ID, FIELD_SERVICE_ID, FIELD_TRIP_ID};

    public final static List<GTFSObjectTrip> allTrips = new ArrayList<>(INITIAL_CAPACITY);
    public final static HashMap<String, GTFSObjectTrip> tripLookup = new HashMap<>(INITIAL_CAPACITY);

    private final static Comparator<GTFSObjectStopTime> stopTimeComparator = new Comparator<GTFSObjectStopTime>() {
        @Override
        public int compare(GTFSObjectStopTime o1, GTFSObjectStopTime o2) {
            if(o1.stop_sequence < o2.stop_sequence)  {
                return -1;
            } else if(o1.stop_sequence > o2.stop_sequence) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    public GTFSTripDirection direction;
    public GTFSObjectRoute parentRoute;
    public GTFSObjectCalendar parentService;
    public GTFSObjectShape shape;
    public final List<GTFSObjectStopTime> stops = new ArrayList<>(INITIAL_CAPACITY_STOPS);

    public void addStopTime(GTFSObjectStopTime stopTime) {
        stops.add(stopTime);
    }

    public GTFSObjectTrip() {
        fields = new HashMap<>(getDefinedFields().length);
    }
    @Override
    protected void addToList() {
        allTrips.add(this);
        tripLookup.put(getField(FIELD_TRIP_ID), this);
    }

    @Override
    public void postProcess() throws InvalidArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            String[] errMsg = {""};
            errMsg[0] = String.format("Missing the following fields: %s", String.join(", ", missingFields));
            throw new InvalidArgumentException(errMsg);
        }

        //process any other fields
        switch (getField(FIELD_DIRECTION_ID)) {
            case DIRECTION_ID_0:
                direction = GTFSTripDirection.direction0;
                break;
            case DIRECTION_ID_1:
                direction = GTFSTripDirection.direction1;
                break;
            default:
                GTFSProcessor.logEvent(GTFSProcessor.LogLevel.error, "Invalid direction " + getField(FIELD_DIRECTION_ID) + " for trip id " + getField(FIELD_TRIP_ID));
                break;
        }
        parentRoute = GTFSObjectRoute.routeLookup.get(getField(FIELD_ROUTE_ID));
        parentService = GTFSObjectCalendar.calendarLookup.get(getField(FIELD_SERVICE_ID));
        parentRoute.addTrip(this);

        if(parentRoute == null) {
            GTFSProcessor.logEvent(GTFSProcessor.LogLevel.warn, "Missing route for trip id " + getField(FIELD_TRIP_ID));
        }
        if(parentService == null) {
            GTFSProcessor.logEvent(GTFSProcessor.LogLevel.warn, "Missing service info for trip id " + getField(FIELD_TRIP_ID));
        }

        String shapeId = getField(FIELD_SHAPE_ID);
        if(shapeId != null) {
            shape = GTFSObjectShape.shapeLookup.get(shapeId);
        } else {
            System.out.println("No shape for trip " + getField(FIELD_TRIP_ID));
        }

        //ensure the stops are ordered by their stop_sequence field
        Collections.sort(stops, stopTimeComparator);

        //add to the main trips list
        addToList();
    }

    @Override
    public String getFileName() {
        return "trips.txt";
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
