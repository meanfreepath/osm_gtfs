package com.company.meanfreepathllc.GTFS;

import com.company.meanfreepathllc.SpatialTypes.Point;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/27/15.
 * TODO: process location_type, parent_station, wheelchair_boarding fields
 */
public class GTFSObjectStop extends GTFSObject {
    public enum LocationType {
        none,
        station
    }

    public final static int INITIAL_CAPACITY = 8192;

    public final static String
            FIELD_STOP_ID = "stop_id",
            FIELD_STOP_CODE = "stop_code",
            FIELD_STOP_NAME = "stop_name",
            FIELD_STOP_DESC = "stop_desc",
            FIELD_STOP_LAT = "stop_lat",
            FIELD_STOP_LON = "stop_lon",
            FIELD_ZONE_ID = "zone_id",
            FIELD_STOP_URL = "stop_url",
            FIELD_LOCATION_TYPE = "location_type",
            FIELD_PARENT_STATION = "parent_station",
            FIELD_STOP_TIMEZONE = "stop_timezone",
            FIELD_WHEELCHAIR_BOARDING = "wheelchair_boarding";

    public final static String[] definedFields = {FIELD_STOP_ID, FIELD_STOP_CODE, FIELD_STOP_NAME, FIELD_STOP_DESC, FIELD_STOP_LAT, FIELD_STOP_LON, FIELD_ZONE_ID, FIELD_STOP_URL, FIELD_LOCATION_TYPE, FIELD_PARENT_STATION, FIELD_STOP_TIMEZONE, FIELD_WHEELCHAIR_BOARDING};
    public final static String[] requiredFields = {FIELD_STOP_ID, FIELD_STOP_NAME, FIELD_STOP_LAT, FIELD_STOP_LON};

    public final static HashMap<String, GTFSObjectStop> stopLookup = new HashMap<>(INITIAL_CAPACITY);

    public Point coordinate;

    public GTFSObjectStop() {
        fields = new HashMap<>(getDefinedFields().length);
    }
    @Override
    protected void addToList() {
        stopLookup.put(getField(FIELD_STOP_ID), this);
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
        coordinate = new Point(fields.get(FIELD_STOP_LAT), fields.get(FIELD_STOP_LON));

        //add to the main stops list
        addToList();
    }
    @Override
    public String getFileName() {
        return "stops.txt";
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
