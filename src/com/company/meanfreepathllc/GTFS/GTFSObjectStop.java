package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.List;

/**
 * Created by nick on 10/27/15.
 * TODO: process location_type, parent_station, wheelchair_boarding fields
 */
public class GTFSObjectStop extends GTFSObject {
    public enum LocationType {
        none,
        station
    };

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

    static {
        String[] df = {FIELD_STOP_ID, FIELD_STOP_CODE, FIELD_STOP_NAME, FIELD_STOP_DESC, FIELD_STOP_LAT, FIELD_STOP_LON, FIELD_ZONE_ID, FIELD_STOP_URL, FIELD_LOCATION_TYPE, FIELD_PARENT_STATION, FIELD_STOP_TIMEZONE, FIELD_WHEELCHAIR_BOARDING};
        definedFields = new String[df.length];
        short i = 0;
        for(String f : df) {
            definedFields[i++] = f;
        }
        String[] rf = {FIELD_STOP_ID, FIELD_STOP_NAME, FIELD_STOP_LAT, FIELD_STOP_LON};
        requiredFields = new String[rf.length];
        i = 0;
        for(String f : rf) {
            requiredFields[i++] = f;
        }
    }

    public double lat, lon;

    @Override
    public void postProcess() throws InvalidArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            String[] errMsg = {""};
            errMsg[0] = String.format("Missing the following fields: %s", String.join(", ", missingFields));
            throw new InvalidArgumentException(errMsg);
        }

        //process any other fields
        lat = Double.parseDouble(fields.get(FIELD_STOP_LAT));
        lon = Double.parseDouble(fields.get(FIELD_STOP_LON));
    }
}
