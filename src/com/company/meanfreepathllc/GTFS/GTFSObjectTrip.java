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
        direction0, direction1, directionNone
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

    private final static Comparator<StopTime> stopTimeComparator = new Comparator<StopTime>() {
        @Override
        public int compare(StopTime o1, StopTime o2) {
            if(o1.stop_sequence < o2.stop_sequence)  {
                return -1;
            } else if(o1.stop_sequence > o2.stop_sequence) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    /**
     * Simple object to map a stop to a sequence number
     */
    public class StopTime {
        public final GTFSObjectStop stop;
        public final short stop_sequence;
        public StopTime(final GTFSObjectStop stop, final short stop_sequence) {
            this.stop = stop;
            this.stop_sequence = stop_sequence;
        }
    }

    public GTFSTripDirection direction;
    public GTFSObjectRoute parentRoute;
    public GTFSObjectCalendar parentService;
    public GTFSObjectShape shape;
    public final List<StopTime> stops = new ArrayList<>();

    public void addStopTime(final GTFSObjectStopTime stopTime, final GTFSDataset dataset) {
        final String stopId = stopTime.getField(GTFSObjectStopTime.FIELD_STOP_ID);
        final GTFSObjectStop stop = dataset.stopLookup.get(stopId);
        if(stop == null) {
            System.out.printf("Warn: stop id %s not found in lookup\n", stopId);
            return;
        }

        //use a pared-down object to track the Stop and its order in this trip, to save memory over the relatively-heavy GTFSObjectStopTime object
        final StopTime stopSequence = new StopTime(stop, Short.parseShort(stopTime.getField(GTFSObjectStopTime.FIELD_STOP_SEQUENCE)));
        stops.add(stopSequence);
    }

    public GTFSObjectTrip() {
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

        //process any other fields
        String directionVal = getField(FIELD_DIRECTION_ID);
        switch (directionVal) {
            case DIRECTION_ID_0:
                direction = GTFSTripDirection.direction0;
                break;
            case DIRECTION_ID_1:
                direction = GTFSTripDirection.direction1;
                break;
            default:
                direction = GTFSTripDirection.directionNone;
                break;
        }
        parentRoute = dataset.routeLookup.get(getField(FIELD_ROUTE_ID));
        parentService = dataset.calendarLookup.get(getField(FIELD_SERVICE_ID));
        parentRoute.addTrip(this);

        if(parentRoute == null) {
            GTFSProcessor.logEvent(GTFSProcessor.LogLevel.warn, "Missing route for trip id " + getField(FIELD_TRIP_ID));
        }
        if(parentService == null) {
            GTFSProcessor.logEvent(GTFSProcessor.LogLevel.warn, "Missing service info for trip id " + getField(FIELD_TRIP_ID));
        }

        String shapeId = getField(FIELD_SHAPE_ID);
        if(shapeId != null) {
            shape = dataset.shapeLookup.get(shapeId);
        } else {
            System.out.println("No shape for trip " + getField(FIELD_TRIP_ID));
        }

        //ensure the stops are ordered by their stop_sequence field
        Collections.sort(stops, stopTimeComparator);

        //add to the main trips list
        dataset.allTrips.add(this);
        dataset.tripLookup.put(getField(FIELD_TRIP_ID), this);
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
