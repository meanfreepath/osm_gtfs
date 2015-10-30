package com.company.meanfreepathllc;

import com.company.meanfreepathllc.GTFS.*;
import com.company.meanfreepathllc.OSM.*;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nick on 10/29/15.
 */
public class DataTransmutator {
    private final static String BAY_REGEX = "[ :-]* bay ([\\S\\d]+)";
    private final static Pattern bayPattern = Pattern.compile(BAY_REGEX, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);

    public static OSMNode transmuteGTFSStop(GTFSObjectStop stopData) {

        //TODO determine correct type for stop (bus, ferry terminal, rail station, etc)
        OSMBusPlatform osmStop = OSMBusPlatform.create();

        //stop_id
        osmStop.setTag(OSMEntity.KEY_REF, stopData.getField(GTFSObjectStop.FIELD_STOP_ID));

        //stop_name
        String stopName = stopData.getField(GTFSObjectStop.FIELD_STOP_NAME);
        Matcher m = bayPattern.matcher(stopName); //Extract the bay from the name, if any, and put in the local_ref tag
        if(m.find()) {
            osmStop.setTag(OSMEntity.KEY_LOCAL_REF, m.group(1));
            stopName = m.replaceAll("");
        }
        osmStop.setTag(OSMEntity.KEY_NAME, stopName);

        //stop_lat, stop_lon
        osmStop.setTag(OSMEntity.KEY_LATITUDE, stopData.getField(GTFSObjectStop.FIELD_STOP_LAT));
        osmStop.setTag(OSMEntity.KEY_LONGITUDE, stopData.getField(GTFSObjectStop.FIELD_STOP_LON));

        return osmStop;
    }
    public static OSMWay transmuteGTFSTrip(GTFSObjectTrip tripData) throws InvalidArgumentException {
        OSMWay shapeWay = OSMWay.create();
        shapeWay.addTag(OSMEntity.KEY_REF, tripData.getField(GTFSObjectTrip.FIELD_TRIP_SHORT_NAME));
        shapeWay.addTag(OSMEntity.KEY_NAME, tripData.getField(GTFSObjectTrip.FIELD_TRIP_HEADSIGN));
        shapeWay.addTag("shape_id", tripData.getField(GTFSObjectTrip.FIELD_SHAPE_ID));
        shapeWay.addTag("direction_id", tripData.getField(GTFSObjectTrip.FIELD_DIRECTION_ID));
        shapeWay.addTag("block_id", tripData.getField(GTFSObjectTrip.FIELD_BLOCK_ID));
        shapeWay.addTag("service_id", tripData.getField(GTFSObjectTrip.FIELD_BLOCK_ID));

        for (GTFSObjectShape.ShapePoint pt : tripData.shape.points) {
            shapeWay.addNode(OSMNode.create(pt));
        }

        return shapeWay;
    }
    public static OSMRelation transmuteGTFSRoute(GTFSObjectRoute routeData) throws InvalidArgumentException {

        OSMRelation osmRoute = OSMRelation.create();

        //agency_id
        osmRoute.setTag(OSMEntity.KEY_OPERATOR, routeData.agency.getField(GTFSObjectAgency.FIELD_AGENCY_NAME));

        //route_id
        osmRoute.setTag("kcm:id", routeData.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME));

        //route_short_name
        osmRoute.setTag(OSMEntity.KEY_REF, routeData.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME));

        //route_long_name
        String routeLongName = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_LONG_NAME);
        if(routeLongName != null && !routeLongName.isEmpty()) {
            osmRoute.setTag(OSMEntity.KEY_NAME, routeLongName);
        } else {
            String ref = osmRoute.getTag(OSMEntity.KEY_REF);
            if(ref == null) {
                osmRoute.setTag(OSMEntity.KEY_NAME, "UNKNOWN ROUTE NUMBER");
            } else {
                osmRoute.setTag(OSMEntity.KEY_NAME, "Route " + ref);
            }
        }
        //route_desc
        processRouteDesc(routeData.getField(GTFSObjectRoute.FIELD_ROUTE_DESC), osmRoute);

        switch(routeData.routeType) {
            case tramStreetcarLightrail:
                osmRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_LIGHT_RAIL); //NOTE: GTFS conflates tram and light_rail types
                break;
            case subwayMetro:
                osmRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_SUBWAY);
            case rail:
                osmRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_TRAIN);
                break;
            case bus:
                osmRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_BUS);
                break;
            case ferry:
                osmRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_FERRY);
                break;
            case cablecar:
                osmRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_TRAM);
                break;
            case gondola:
                osmRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_AERIALWAY);
                break;
            case funicular:
                osmRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_TRAIN);
                break;
        }

        //route_url
        String routeUrl = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_URL);
        if(routeUrl != null && !routeUrl.isEmpty()) {
            osmRoute.setTag(OSMEntity.KEY_WEBSITE, routeUrl);
        }

        //route_color
        String routeColor = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_COLOR);
        if(routeColor != null && !routeColor.isEmpty()) {
            osmRoute.setTag(OSMEntity.KEY_COLOUR, routeColor);
        }


        //now compile the relation members: first combine trips by shape id
        HashMap<String, GTFSObjectTrip> tripsByShape = new HashMap<>(routeData.trips.size());
        for(GTFSObjectTrip trip: routeData.trips) {
            if(trip.shape != null) {
                tripsByShape.put(trip.getField(GTFSObjectTrip.FIELD_SHAPE_ID), trip);
            }
        }

        //and add as members to the relation
        OSMWay shapeWay;
        OSMBusPlatform stop;
        for(GTFSObjectTrip trip: tripsByShape.values()) {
            if(trip.shape != null) {
                shapeWay = transmuteGTFSTrip(trip);
                osmRoute.addMember(shapeWay, "");

                //also compile the stops and add them to the relation
                for(GTFSObjectTrip.StopTime stopTime: trip.stops) {
                    stop = OSMBusPlatform.create();
                    GTFSObjectStop stopData = stopTime.stop;
                    stop.setTag(OSMEntity.KEY_REF, stopData.getField(GTFSObjectStop.FIELD_STOP_ID));
                    stop.setTag(OSMEntity.KEY_NAME, stopData.getField(GTFSObjectStop.FIELD_STOP_NAME));
                    stop.setTag(OSMEntity.KEY_LATITUDE, stopData.getField(GTFSObjectStop.FIELD_STOP_LAT));
                    stop.setTag(OSMEntity.KEY_LONGITUDE, stopData.getField(GTFSObjectStop.FIELD_STOP_LON));
                    stop.setTag(OSMEntity.KEY_LONGITUDE, stopData.getField(GTFSObjectStop.FIELD_STOP_LON));
                    osmRoute.addMember(stop, "stop");
                }
            }
        }


        return osmRoute;
    }

    private static void processRouteDesc(String value, OSMRelation route) {
        final String[] splitters = {" to ", " - ", "/"};
        if(value != null) {
            boolean wasSplit = false;
            for(final String spl : splitters) {
                if(value.contains(spl)) {
                    final String[] split = value.split(spl);
                    wasSplit = true;
                    if(split.length == 1) {
                        route.setTag(OSMEntity.KEY_DESCRIPTION, value);
                    } else if(split.length == 2) { //from-to
                        route.setTag(OSMEntity.KEY_FROM, split[0]);
                        route.setTag(OSMEntity.KEY_TO, split[1]);
                    } else if(split.length >= 3) { //i.e "via" info
                        route.setTag(OSMEntity.KEY_FROM, split[0]);
                        ArrayList<String> via = new ArrayList<>(split.length - 2);
                        via.addAll(Arrays.asList(split).subList(1, split.length - 1));
                        route.setTag(OSMEntity.KEY_VIA, String.join(";", via));
                        route.setTag(OSMEntity.KEY_TO, split[split.length - 1]);
                    }
                }
            }

            if(!wasSplit) {
                route.setTag(OSMEntity.KEY_DESCRIPTION, value);
            }
        }
    }
}
