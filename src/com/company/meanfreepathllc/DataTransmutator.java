package com.company.meanfreepathllc;

import com.company.meanfreepathllc.GTFS.*;
import com.company.meanfreepathllc.OSM.*;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nick on 10/29/15.
 */
public class DataTransmutator {
    private final static int INITIAL_CAPACITY = 65536;
    private final static String BAY_REGEX = "[ :-]* bay ([\\S\\d]+)";
    private final static Pattern bayPattern = Pattern.compile(BAY_REGEX, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);

    public final HashMap<Integer, OSMEntity> allEntities = new HashMap<>(INITIAL_CAPACITY);

    public OSMWay transmuteGTFSTrip(GTFSObjectTrip tripData) throws InvalidArgumentException {
        OSMWay shapeWay = (OSMWay) osmEntityForGTFSObject(tripData.shape, OSMWay.class);
      //  System.out.println("Trip " + tripData.getField(GTFSObjectTrip.FIELD_TRIP_ID) + ": " + tripData.fields.toString());
        String tripRef = tripData.getField(GTFSObjectTrip.FIELD_TRIP_SHORT_NAME);
        if(tripRef == null) {
            tripRef = tripData.getField(GTFSObjectTrip.FIELD_TRIP_ID);
        }
        shapeWay.addTag(OSMEntity.KEY_REF, tripRef);
        shapeWay.addTag(OSMEntity.KEY_NAME, tripData.getField(GTFSObjectTrip.FIELD_TRIP_HEADSIGN));

        for (GTFSObjectShape.ShapePoint pt : tripData.shape.points) {
            shapeWay.addNode(OSMNode.create(pt));
        }

        return shapeWay;
    }
    public OSMRelation transmuteGTFSRoute(GTFSObjectRoute routeData) throws InvalidArgumentException {
        OSMRouteMaster osmRouteMaster = (OSMRouteMaster) osmEntityForGTFSObject(routeData, OSMRouteMaster.class);

        //agency_id
        osmRouteMaster.setTag(OSMEntity.KEY_OPERATOR, routeData.agency.getField(GTFSObjectAgency.FIELD_AGENCY_NAME));

        //route_id
        osmRouteMaster.setTag("kcm:id", routeData.getField(GTFSObjectRoute.FIELD_ROUTE_ID));

        //route_short_name
        osmRouteMaster.setTag(OSMEntity.KEY_REF, routeData.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME));

        //route_long_name
        String routeLongName = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_LONG_NAME);
        if(routeLongName != null && !routeLongName.isEmpty()) {
            osmRouteMaster.setTag(OSMEntity.KEY_NAME, routeLongName);
        } else {
            String ref = osmRouteMaster.getTag(OSMEntity.KEY_REF);
            if(ref == null) {
                osmRouteMaster.setTag(OSMEntity.KEY_NAME, "UNKNOWN ROUTE NUMBER");
            } else {
                osmRouteMaster.setTag(OSMEntity.KEY_NAME, "Route " + ref);
            }
        }
        //route_desc
        processRouteDesc(routeData.getField(GTFSObjectRoute.FIELD_ROUTE_DESC), osmRouteMaster);

        final String[] debugRouteWay = {"", ""};
        switch(routeData.routeType) {
            case tramStreetcarLightrail:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_LIGHT_RAIL); //NOTE: GTFS conflates tram and light_rail types
                debugRouteWay[0] = "railway";
                debugRouteWay[1] = "light_rail";
                break;
            case subwayMetro:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_SUBWAY);
                debugRouteWay[0] = "railway";
                debugRouteWay[1] = "subway";
            case rail:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_TRAIN);
                debugRouteWay[0] = "railway";
                debugRouteWay[1] = "rail";
                break;
            case bus:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_BUS);
                debugRouteWay[0] = "highway";
                debugRouteWay[1] = "road";
                break;
            case ferry:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_FERRY);
                debugRouteWay[0] = "route";
                debugRouteWay[1] = "ferry";
                break;
            case cablecar:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_TRAM);
                debugRouteWay[0] = "railway";
                debugRouteWay[1] = "tram";
                break;
            case gondola:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_AERIALWAY);
                debugRouteWay[0] = "aerialway";
                debugRouteWay[1] = "gondola";
                break;
            case funicular:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_TRAIN);
                debugRouteWay[0] = "railway";
                debugRouteWay[1] = "rail";
                break;
        }

        //route_url
        String routeUrl = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_URL);
        if(routeUrl != null && !routeUrl.isEmpty()) {
            osmRouteMaster.setTag(OSMEntity.KEY_WEBSITE, routeUrl);
        }

        //route_color
        String routeColor = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_COLOR);
        if(routeColor != null && !routeColor.isEmpty()) {
            osmRouteMaster.setTag(OSMEntity.KEY_COLOUR, routeColor);
        }

        //now compile the relation members: first pick the longest trip in each direction
        HashMap<String, GTFSObjectTrip> tripsToUse = new HashMap<>(8);
        GTFSObjectTrip maxGroupTrip;
        final String groupingFormat = "%d:%s";
        String groupingId;
        for(GTFSObjectTrip trip: routeData.trips) {
            if(trip.shape != null) {
                groupingId = String.format(groupingFormat, trip.direction.ordinal(), trip.getField(GTFSObjectTrip.FIELD_TRIP_SHORT_NAME));
                maxGroupTrip = tripsToUse.get(groupingId);
                if(maxGroupTrip == null || trip.shape.totalDistanceTraveled > maxGroupTrip.shape.totalDistanceTraveled) {
                    tripsToUse.put(groupingId, trip);
                }
            }
        }

        //and add as members to the relation
        OSMRoute tripRoute;
        OSMWay shapeWay;
        for(GTFSObjectTrip trip: tripsToUse.values()) {
            if(trip.shape != null) {
                //init the OSM route relation for the trip and set the tags as needed
                tripRoute = (OSMRoute) osmEntityForGTFSObject(trip, OSMRoute.class);
                tripRoute.setTag("kcm:id", trip.getField(GTFSObjectTrip.FIELD_TRIP_ID));
                tripRoute.setTag("kcm:shapeid", trip.getField(GTFSObjectTrip.FIELD_SHAPE_ID));
                tripRoute.setTag(OSMEntity.KEY_NAME, trip.getField(GTFSObjectTrip.FIELD_TRIP_HEADSIGN));
                tripRoute.setTag(OSMEntity.KEY_REF, osmRouteMaster.getTag(OSMEntity.KEY_REF));
                tripRoute.setTag(OSMEntity.KEY_ROUTE, osmRouteMaster.getTag(OSMEntity.KEY_ROUTE_MASTER));
                tripRoute.setTag(OSMEntity.KEY_OPERATOR, osmRouteMaster.getTag(OSMEntity.KEY_OPERATOR));

                //add the shape (which details the path of the route) for later application to OSM highways
                shapeWay = transmuteGTFSTrip(trip);
                shapeWay.setTag(debugRouteWay[0], debugRouteWay[1]);
                tripRoute.addMember(shapeWay, "");

                //also compile the stops and add them to the relation
                for(GTFSObjectStopTime stopTime: trip.stops) {
                    processStopForRoute(stopTime.stop, routeData, tripRoute);
                }

                //and add the trip to the route master
                osmRouteMaster.addRoute(tripRoute);
            }
        }
        return osmRouteMaster;
    }
    private OSMEntity osmEntityForGTFSObject(GTFSObject originalObject, Class<? extends OSMEntity> createClass) {
        OSMEntity entity = allEntities.get(originalObject.internalId);
        if(entity == null && createClass != null) {
            try {
                Method createMethod = createClass.getMethod("create");
                entity = (OSMEntity) createMethod.invoke(null);
                addEntity(originalObject, entity);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return entity;
    }
    private void addEntity(GTFSObject originalObject, OSMEntity entity) {
        allEntities.put(originalObject.internalId, entity);
    }
    private void processStopForRoute(GTFSObjectStop stopData, GTFSObjectRoute route, OSMRelation osmRoute) {
        OSMEntity osmStop;

        switch(route.routeType) {
            case tramStreetcarLightrail: //NOTE: GTFS conflates tram and light_rail types
                osmStop = osmEntityForGTFSObject(stopData, OSMPlatform.class);
                osmStop.setTag(OSMPublicTransport.KEY_TRAIN, OSMEntity.TAG_YES);
                break;
            case subwayMetro:
                osmStop = osmEntityForGTFSObject(stopData, OSMPlatform.class);
                osmStop.setTag(OSMPublicTransport.KEY_TRAIN, OSMEntity.TAG_YES);
            case rail:
                osmStop = osmEntityForGTFSObject(stopData, OSMPlatform.class);
                osmStop.setTag(OSMPublicTransport.KEY_TRAIN, OSMEntity.TAG_YES);
                break;
            case bus:
                osmStop = osmEntityForGTFSObject(stopData, OSMPlatform.class);
                osmStop.setTag(OSMPublicTransport.KEY_BUS, OSMPublicTransport.TAG_YES);
                osmStop.setTag(OSMPublicTransport.KEY_HIGHWAY, OSMPublicTransport.TAG_BUS_STOP);
                break;
            case ferry:
                osmStop = osmEntityForGTFSObject(stopData, OSMStopPosition.class);
                osmStop.setTag(OSMPublicTransport.KEY_FERRY, OSMEntity.TAG_YES);
                osmStop.setTag(OSMPublicTransport.KEY_AMENITY, OSMPublicTransport.TAG_LEGACY_FERRY_TERMINAL);
                break;
            case cablecar:
                osmStop = osmEntityForGTFSObject(stopData, OSMPlatform.class);
                osmStop.setTag(OSMPublicTransport.KEY_TRAM, OSMEntity.TAG_YES);
                break;
            case gondola:
                osmStop = osmEntityForGTFSObject(stopData, OSMPlatform.class);
                osmStop.setTag(OSMPublicTransport.KEY_AERIALWAY, OSMEntity.TAG_YES);
                break;
            case funicular:
                osmStop = osmEntityForGTFSObject(stopData, OSMPlatform.class);
                osmStop.setTag(OSMPublicTransport.KEY_FUNICULAR, OSMEntity.TAG_YES);
                break;
            default: //shouldn't happen (all cases handled)
                osmStop = osmEntityForGTFSObject(stopData, OSMPlatform.class);
                break;
        }

        //tags common to all stops
        osmStop.setTag(OSMEntity.KEY_REF, stopData.getField(GTFSObjectStop.FIELD_STOP_ID));
        osmStop.setTag(OSMEntity.KEY_NAME, stopData.getField(GTFSObjectStop.FIELD_STOP_NAME));

        String stopName = stopData.getField(GTFSObjectStop.FIELD_STOP_NAME);
        Matcher m = bayPattern.matcher(stopName); //Extract the bay from the name, if any, and put in the local_ref tag
        if(m.find()) {
            osmStop.setTag(OSMEntity.KEY_LOCAL_REF, m.group(1));
            stopName = m.replaceAll("");
        }
        osmStop.setTag(OSMEntity.KEY_NAME, stopName);

        osmStop.setTag(OSMEntity.KEY_LATITUDE, stopData.getField(GTFSObjectStop.FIELD_STOP_LAT));
        osmStop.setTag(OSMEntity.KEY_LONGITUDE, stopData.getField(GTFSObjectStop.FIELD_STOP_LON));
        String stopDescription = stopData.getField(GTFSObjectStop.FIELD_STOP_DESC);
        if(stopDescription != null && !stopDescription.isEmpty()) {
            osmStop.setTag(OSMEntity.KEY_DESCRIPTION, stopDescription);
        }
        String wheelchairs = stopData.getField(GTFSObjectStop.FIELD_WHEELCHAIR_BOARDING);
        if(wheelchairs != null && !wheelchairs.isEmpty()) {
            osmStop.setTag(OSMEntity.KEY_WHEELCHAIR, OSMEntity.TAG_YES);
        }
        String stopWebsite = stopData.getField(GTFSObjectStop.FIELD_STOP_URL);
        if(stopWebsite != null && !stopWebsite.isEmpty()) {
            osmStop.setTag(OSMEntity.KEY_WEBSITE, stopWebsite);
        }

        osmRoute.addMember(osmStop, osmStop instanceof OSMStopPosition ? "stop" : "platform");
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
