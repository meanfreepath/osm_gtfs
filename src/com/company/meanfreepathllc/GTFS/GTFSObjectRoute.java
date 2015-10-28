package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 10/27/15.
 */
public class GTFSObjectRoute extends GTFSObject {
    public final static String
            FIELD_ROUTE_ID = "route_id",
            FIELD_AGENCY_ID = "agency_id",
            FIELD_ROUTE_SHORT_NAME = "route_short_name",
            FIELD_ROUTE_LONG_NAME = "route_long_name",
            FIELD_ROUTE_DESC = "route_desc",
            FIELD_ROUTE_TYPE = "route_type",
            FIELD_ROUTE_URL = "route_url",
            FIELD_ROUTE_COLOR = "route_color",
            FIELD_ROUTE_TEXT_COLOR = "route_text_color";

    /**
     * Defined at https://developers.google.com/transit/gtfs/reference#routes_fields
     */
    public enum GTFSRouteType {
        tramStreetcarLightrail, //Tram, Streetcar, Light rail. Any light rail or street level system within a metropolitan area.
        subwayMetro, //Subway, Metro. Any underground rail system within a metropolitan area.
        rail, //Rail. Used for intercity or long-distance travel.
        bus, //Bus. Used for short- and long-distance bus routes.
        ferry, //Ferry. Used for short- and long-distance boat service.
        cablecar, //Cable car. Used for street-level cable cars where the cable runs beneath the car.
        gondola, //Gondola, Suspended cable car. Typically used for aerial cable cars where the car is suspended from the cable.
        funicular //Funicular. Any rail system designed for steep inclines.
    };
    static {
        String[] df = {FIELD_ROUTE_ID, FIELD_AGENCY_ID, FIELD_ROUTE_SHORT_NAME, FIELD_ROUTE_LONG_NAME, FIELD_ROUTE_DESC, FIELD_ROUTE_TYPE, FIELD_ROUTE_URL, FIELD_ROUTE_COLOR, FIELD_ROUTE_TEXT_COLOR};
        definedFields = new String[df.length];
        short i = 0;
        for(String f : df) {
            definedFields[i++] = f;
        }
        String[] rf = {FIELD_ROUTE_ID, FIELD_ROUTE_SHORT_NAME, FIELD_ROUTE_LONG_NAME, FIELD_ROUTE_TYPE};
        requiredFields = new String[rf.length];
        i = 0;
        for(String f : rf) {
            requiredFields[i++] = f;
        }
    }

    public int routeType;
    public GTFSObjectAgency agency;

    @Override
    public void postProcess() throws InvalidArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            String[] errMsg = {""};
            errMsg[0] = String.format("Missing the following fields: %s", String.join(", ", missingFields));
            throw new InvalidArgumentException(errMsg);
        }

        //now add any processed values
        routeType = Integer.parseInt(fields.get(FIELD_ROUTE_TYPE));
        agency = GTFSAgencyProcessor.instance.lookupAgencyById(fields.get(FIELD_AGENCY_ID));
    }
}
