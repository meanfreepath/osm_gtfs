package com.company.meanfreepathllc;

import com.company.meanfreepathllc.GTFS.*;
import com.company.meanfreepathllc.OSM.*;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nick on 10/29/15.
 */
public class DataTransmutator {
    private final static String KEY_GTFS_AGENCY_ID = "gtfs:agency_id", KEY_GTFS_DATASET_ID = "gtfs:dataset_id", KEY_GTFS_STOP_ID = "gtfs:stop_id", KEY_GTFS_ROUTE_ID = "gtfs:route_id", KEY_GTFS_TRIP_ID = "gtfs:trip_id", KEY_GTFS_SHAPE_ID = "gtfs:shape_id";
    private final static int INITIAL_CAPACITY = 262144;
    private final static String BAY_REGEX = "[ :-]* bay ([\\S\\d]+)";
    private final static Pattern bayPattern = Pattern.compile(BAY_REGEX, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);

    public final String datasetId, datasetSource;

    /**
     * Tracks the GTFS entities that have been added to the OSM entity space, to avoid duplication
     */
    private final HashMap<Integer, OSMEntity> allGTFSEntities = new HashMap<>(INITIAL_CAPACITY);
    private final OSMEntitySpace outputEntitySpace;

    public DataTransmutator(final String datasetId, final String datasetSource) {
        this.datasetId = datasetId;
        this.datasetSource = datasetSource;
        outputEntitySpace = new OSMEntitySpace(INITIAL_CAPACITY);
    }

    private OSMWay transmuteGTFSTrip(GTFSObjectTrip tripData) {
        final OSMWay shapeWay = outputEntitySpace.createWay(null, null);
      //  System.out.println("Trip " + tripData.getField(GTFSObjectTrip.FIELD_TRIP_ID) + ": " + tripData.fields.toString());
        String tripRef = tripData.getField(GTFSObjectTrip.FIELD_TRIP_SHORT_NAME);
        if(tripRef == null) {
            tripRef = tripData.getField(GTFSObjectTrip.FIELD_TRIP_ID);
        }

        shapeWay.setTag(KEY_GTFS_DATASET_ID, datasetId);
        shapeWay.setTag(OSMEntity.KEY_SOURCE, datasetSource);
        shapeWay.setTag(KEY_GTFS_SHAPE_ID, tripData.getField(GTFSObjectTrip.FIELD_SHAPE_ID));
        shapeWay.setTag(OSMEntity.KEY_REF, tripRef);
        shapeWay.setTag(OSMEntity.KEY_NAME, tripData.getField(GTFSObjectTrip.FIELD_TRIP_HEADSIGN));

        for (final GTFSObjectShape.ShapePoint pt : tripData.shape.points) {
            shapeWay.appendNode(outputEntitySpace.createNode(pt.latitude, pt.longitude, null));
        }

        outputEntitySpace.addEntity(shapeWay, OSMEntity.TagMergeStrategy.keepTags, null);
        return shapeWay;
    }
    public OSMRelation transmuteGTFSRoute(GTFSObjectRoute routeData) throws InvalidArgumentException {
        final OSMRelation osmRouteMaster = outputEntitySpace.createRelation(null, null);
        OSMPresetFactory.makeRouteMaster(osmRouteMaster);

        //agency_id
        osmRouteMaster.setTag(KEY_GTFS_DATASET_ID, datasetId);
        osmRouteMaster.setTag(OSMEntity.KEY_SOURCE, datasetSource);
        osmRouteMaster.setTag(KEY_GTFS_AGENCY_ID, routeData.agency.getField(GTFSObjectAgency.FIELD_AGENCY_ID));
        osmRouteMaster.setTag(OSMEntity.KEY_OPERATOR, routeData.agency.getField(GTFSObjectAgency.FIELD_AGENCY_NAME));

        //route_id
        osmRouteMaster.setTag(KEY_GTFS_ROUTE_ID, routeData.getField(GTFSObjectRoute.FIELD_ROUTE_ID));

        //route_short_name
        osmRouteMaster.setTag(OSMEntity.KEY_REF, routeData.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME));

        //route_long_name
        final String routeLongName = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_LONG_NAME);
        if(routeLongName != null && !routeLongName.isEmpty()) {
            osmRouteMaster.setTag(OSMEntity.KEY_NAME, routeLongName);
        } else {
            final String ref = osmRouteMaster.getTag(OSMEntity.KEY_REF);
            if(ref == null) {
                osmRouteMaster.setTag(OSMEntity.KEY_NAME, "UNKNOWN ROUTE NUMBER");
            } else {
                osmRouteMaster.setTag(OSMEntity.KEY_NAME, "Route " + ref);
            }
        }
        //route_desc
        processRouteDesc(routeData.getField(GTFSObjectRoute.FIELD_ROUTE_DESC), osmRouteMaster);

        final String[] routeWayTags = {"", ""};
        switch(routeData.routeType) {
            case tramStreetcarLightrail:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_LIGHT_RAIL); //NOTE: GTFS conflates tram and light_rail types
                routeWayTags[0] = "railway";
                routeWayTags[1] = "light_rail";
                break;
            case subwayMetro:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_SUBWAY);
                routeWayTags[0] = "railway";
                routeWayTags[1] = "subway";
            case rail:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_TRAIN);
                routeWayTags[0] = "railway";
                routeWayTags[1] = "rail";
                break;
            case bus:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_BUS);
                routeWayTags[0] = "highway";
                routeWayTags[1] = "road";
                break;
            case ferry:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_FERRY);
                routeWayTags[0] = "route";
                routeWayTags[1] = "ferry";
                break;
            case cablecar:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_TRAM);
                routeWayTags[0] = "railway";
                routeWayTags[1] = "tram";
                break;
            case gondola:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_AERIALWAY);
                routeWayTags[0] = "aerialway";
                routeWayTags[1] = "gondola";
                break;
            case funicular:
                osmRouteMaster.setTag(OSMEntity.KEY_ROUTE_MASTER, OSMEntity.TAG_TRAIN);
                routeWayTags[0] = "railway";
                routeWayTags[1] = "rail";
                break;
        }

        //route_url
        final String routeUrl = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_URL);
        if(routeUrl != null && !routeUrl.isEmpty()) {
            osmRouteMaster.setTag(OSMEntity.KEY_WEBSITE, routeUrl);
        }

        //route_color
        final String routeColor = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_COLOR);
        if(routeColor != null && !routeColor.isEmpty()) {
            osmRouteMaster.setTag(OSMEntity.KEY_COLOUR, routeColor);
        }

        //now compile the relation members: first pick the longest trip in each direction
        final HashMap<String, GTFSObjectTrip> tripsToUse = new HashMap<>(8);
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
        OSMRelation tripRoute;
        OSMWay shapeWay;
        for(GTFSObjectTrip trip: tripsToUse.values()) {
            if(trip.shape != null) {
                //init the OSM route relation for the trip and set the tags as needed
                tripRoute = outputEntitySpace.createRelation(null, null);
                OSMPresetFactory.makeRoute(tripRoute);
                tripRoute.setTag(KEY_GTFS_DATASET_ID, datasetId);
                tripRoute.setTag(OSMEntity.KEY_SOURCE, datasetSource);
                tripRoute.setTag(KEY_GTFS_TRIP_ID, trip.getField(GTFSObjectTrip.FIELD_TRIP_ID));
                tripRoute.setTag(KEY_GTFS_ROUTE_ID, trip.getField(GTFSObjectTrip.FIELD_ROUTE_ID));
                tripRoute.setTag(KEY_GTFS_SHAPE_ID, trip.getField(GTFSObjectTrip.FIELD_SHAPE_ID));
                tripRoute.setTag(OSMEntity.KEY_NAME, trip.getField(GTFSObjectTrip.FIELD_TRIP_HEADSIGN));
                tripRoute.setTag(OSMEntity.KEY_REF, osmRouteMaster.getTag(OSMEntity.KEY_REF));
                tripRoute.setTag(OSMEntity.KEY_ROUTE, osmRouteMaster.getTag(OSMEntity.KEY_ROUTE_MASTER));
                tripRoute.setTag(OSMEntity.KEY_OPERATOR, osmRouteMaster.getTag(OSMEntity.KEY_OPERATOR));

                //add the shape (which details the path of the route) for later application to OSM highways
                shapeWay = transmuteGTFSTrip(trip);
                shapeWay.setTag(routeWayTags[0], routeWayTags[1]);
                tripRoute.addMember(shapeWay, "");

                //also compile the stops and add them to the relation
                for(GTFSObjectStopTime stopTime: trip.stops) {
                    processStopForRoute(stopTime.stop, routeData, tripRoute);
                }

                //and add the trip to the route master
                osmRouteMaster.addMember(tripRoute, OSMEntity.MEMBERSHIP_DEFAULT);
            }
        }
        return osmRouteMaster;
    }
    private OSMNode createStopNode(final GTFSObjectStop gtfsObject) {
        OSMNode existingEntity = (OSMNode) allGTFSEntities.get(gtfsObject.internalId);
        if(existingEntity == null) {
            existingEntity = outputEntitySpace.createNode(gtfsObject.coordinate.latitude, gtfsObject.coordinate.longitude, null);
            allGTFSEntities.put(gtfsObject.internalId, existingEntity);
        }
        return existingEntity;
    }
    private void processStopForRoute(GTFSObjectStop stopData, GTFSObjectRoute route, OSMRelation osmRoute) {
        final OSMEntity osmStop;

        osmStop = createStopNode(stopData);
        switch(route.routeType) {
            case tramStreetcarLightrail: //NOTE: GTFS conflates tram and light_rail types
                OSMPresetFactory.makePlatform(osmStop);
                osmStop.setTag(OSMEntity.KEY_TRAIN, OSMEntity.TAG_YES);
                break;
            case subwayMetro:
                OSMPresetFactory.makeSubwayPlatform(osmStop);
                break;
            case rail:
                OSMPresetFactory.makeTrainPlatform(osmStop);
                break;
            case bus:
                OSMPresetFactory.makeBusPlatform(osmStop);
                break;
            case ferry:
                OSMPresetFactory.makePlatform(osmStop);
                osmStop.setTag(OSMEntity.KEY_FERRY, OSMEntity.TAG_YES);
                osmStop.setTag(OSMEntity.KEY_AMENITY, OSMEntity.TAG_LEGACY_FERRY_TERMINAL);
                break;
            case cablecar:
                OSMPresetFactory.makePlatform(osmStop);
                osmStop.setTag(OSMEntity.KEY_TRAM, OSMEntity.TAG_YES);
                break;
            case gondola:
                OSMPresetFactory.makePlatform(osmStop);
                osmStop.setTag(OSMEntity.KEY_AERIALWAY, OSMEntity.TAG_YES);
                break;
            case funicular:
                OSMPresetFactory.makePlatform(osmStop);
                osmStop.setTag(OSMEntity.KEY_FUNICULAR, OSMEntity.TAG_YES);
                break;
            default: //shouldn't happen (all cases handled)
                break;
        }

        //tags common to all stops
        osmStop.setTag(KEY_GTFS_DATASET_ID, datasetId);
        osmStop.setTag(OSMEntity.KEY_SOURCE, datasetSource);
        osmStop.setTag(KEY_GTFS_STOP_ID, stopData.getField(GTFSObjectStop.FIELD_STOP_ID));
        osmStop.setTag(OSMEntity.KEY_REF, stopData.getField(GTFSObjectStop.FIELD_STOP_ID));
        osmStop.setTag(OSMEntity.KEY_NAME, stopData.getField(GTFSObjectStop.FIELD_STOP_NAME));

        String stopName = stopData.getField(GTFSObjectStop.FIELD_STOP_NAME);
        Matcher m = bayPattern.matcher(stopName); //Extract the bay from the name, if any, and put in the local_ref tag
        if(m.find()) {
            osmStop.setTag(OSMEntity.KEY_LOCAL_REF, m.group(1));
            stopName = m.replaceAll("");
        }
        osmStop.setTag(OSMEntity.KEY_NAME, stopName);

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

        osmRoute.addMember(osmStop, OSMEntity.TAG_STOP_POSITION.equals(osmStop.getTag(OSMEntity.KEY_PUBLIC_TRANSPORT)) ? "stop" : "platform");
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
    public void outputToOSMXML(final String fileName) throws IOException {
        outputEntitySpace.markAllEntitiesWithAction(OSMEntity.ChangeAction.none);
        outputEntitySpace.outputXml(fileName);
    }
}
