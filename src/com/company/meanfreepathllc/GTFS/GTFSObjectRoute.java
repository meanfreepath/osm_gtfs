package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/27/15.
 */
public class GTFSObjectRoute extends GTFSObject {
    public final static int INITIAL_CAPACITY = 256, INITIAL_CAPACITY_TRIP = 2048;

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

    public final static String[] definedFields = {FIELD_ROUTE_ID, FIELD_AGENCY_ID, FIELD_ROUTE_SHORT_NAME, FIELD_ROUTE_LONG_NAME, FIELD_ROUTE_DESC, FIELD_ROUTE_TYPE, FIELD_ROUTE_URL, FIELD_ROUTE_COLOR, FIELD_ROUTE_TEXT_COLOR};
    public final static String[] requiredFields = {FIELD_ROUTE_ID, FIELD_ROUTE_SHORT_NAME, FIELD_ROUTE_LONG_NAME, FIELD_ROUTE_TYPE};

    public final static List<GTFSObjectRoute> allRoutes = new ArrayList<>(INITIAL_CAPACITY);
    public final static HashMap<String, GTFSObjectRoute> routeLookup = new HashMap<>(INITIAL_CAPACITY);

    public GTFSRouteType routeType;
    public GTFSObjectAgency agency;

    public List<GTFSObjectTrip> trips = new ArrayList<>(INITIAL_CAPACITY_TRIP);

    public void addTrip(GTFSObjectTrip trip) throws InvalidArgumentException {
        if(!trip.getField(GTFSObjectTrip.FIELD_ROUTE_ID).equals(getField(FIELD_ROUTE_ID))) {
            String[] errMsg = {""};
            errMsg[0] = String.format("Trip id %s doesn’t match Route id %s", trip.getField(GTFSObjectTrip.FIELD_ROUTE_ID), getField(FIELD_ROUTE_ID));
            throw new InvalidArgumentException(errMsg);
        }
        trips.add(trip);
    }

    @Override
    public void postProcess() throws InvalidArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            String[] errMsg = {""};
            errMsg[0] = String.format("Missing the following fields: %s", String.join(", ", missingFields));
            throw new InvalidArgumentException(errMsg);
        }

        //now add any processed values
        int rawRouteType = Integer.parseInt(fields.get(FIELD_ROUTE_TYPE));
        switch (rawRouteType) {
            case 0:
                routeType = GTFSRouteType.tramStreetcarLightrail;
                break;
            case 1:
                routeType = GTFSRouteType.subwayMetro;
                break;
            case 2:
                routeType = GTFSRouteType.rail;
                break;
            case 3:
                routeType = GTFSRouteType.bus;
                break;
            case 4:
                routeType = GTFSRouteType.ferry;
                break;
            case 5:
                routeType = GTFSRouteType.cablecar;
                break;
            case 6:
                routeType = GTFSRouteType.gondola;
                break;
            case 7:
                routeType = GTFSRouteType.funicular;
                break;
            default:
                String[] errMsg = {"Invalid route type " + rawRouteType};
                throw new InvalidArgumentException(errMsg);
        }

        agency = GTFSObjectAgency.lookupAgencyById(fields.get(FIELD_AGENCY_ID));


        //add to the main route list
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
        allRoutes.add(this);
        routeLookup.put(getField(FIELD_ROUTE_ID), this);
    }
}
