package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/28/15.
 */
public class GTFSObjectTrip extends GTFSObject {
    public final static int INITIAL_CAPACITY = 65536, INITIAL_CAPACITY_STOPS = 128;

    public class StopTime {
        GTFSObjectStop stop;

        public StopTime(GTFSObjectStop stop) {
            this.stop = stop;
        }
    };

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

    public GTFSObjectRoute parentRoute;
    public GTFSObjectCalendar parentService;
    public GTFSObjectShape shape;
    public List<StopTime> stops = new ArrayList<>(INITIAL_CAPACITY_STOPS);

    public void addStopTime(GTFSObjectStopTime stopTime) {
        stops.add(new StopTime(stopTime.stop));
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
        parentRoute = GTFSObjectRoute.routeLookup.get(getField(FIELD_ROUTE_ID));
        parentService = GTFSObjectCalendar.calendarLookup.get(getField(FIELD_SERVICE_ID));
        parentRoute.addTrip(this);

        String shapeId = getField(FIELD_SHAPE_ID);
        if(shapeId != null) {
            shape = GTFSObjectShape.shapeLookup.get(shapeId);
        }


        //add to the main trips list
        addToList();
    }
}
