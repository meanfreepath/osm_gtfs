package GTFSConverter;

import GTFSConverter.GTFS.*;
import GTFSConverter.OSM.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nick on 10/29/15.
 */
public class DataTransmutator {
    private final static String KEY_GTFS_AGENCY_ID = "gtfs:agency_id", KEY_GTFS_DATASET_ID = "gtfs:dataset_id", KEY_GTFS_STOP_ID = "gtfs:stop_id", KEY_GTFS_ROUTE_ID = "gtfs:route_id", KEY_GTFS_TRIP_ID = "gtfs:trip_id", KEY_GTFS_TRIP_MARKER = "gtfs:trip_marker", KEY_GTFS_SHAPE_ID = "gtfs:shape_id";
    private final static int INITIAL_CAPACITY = 262144;
    private final static String BAY_REGEX = "[ :-]* bay ([\\S\\d]+)";
    private final static Pattern bayPattern = Pattern.compile(BAY_REGEX, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
    private final static String TRIP_NAME_FORMAT = "Route %s: %s";

    public final String datasetId, datasetSource;

    private void setRefForTrip(final GTFSObjectRoute route, final GTFSObjectTrip trip, final OSMEntity tripEntity) {
        final String ref = route.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME);
        if(ref == null || ref.isEmpty()) {
            return;
        }

        //express routes add an "X" to their ref tags
        final String tripRef;
        if("express".equalsIgnoreCase(ref)) {
            tripRef = ref + "X";
        } else {
            tripRef = ref;
        }

        tripEntity.setTag(OSMEntity.KEY_REF, tripRef);
    }
    private void setRefForRoute(final GTFSObjectRoute route, final OSMEntity routeEntity) {
        final String ref = route.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME);
        if(ref != null && !ref.isEmpty()) {
            routeEntity.setTag(OSMEntity.KEY_REF, ref);
        }
    }
    private void setNameForTrip(final GTFSObjectRoute route, final GTFSObjectTrip trip, final OSMEntity tripEntity) {
        //final String name = route.getField(GTFSObjectRoute.FIELD_ROUTE_LONG_NAME); // TriMet
        final String name = String.format(TRIP_NAME_FORMAT, route.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME), trip.getField(GTFSObjectTrip.FIELD_TRIP_HEADSIGN)); //King County Metro
        if(name != null && !name.isEmpty()) {
            tripEntity.setTag(OSMEntity.KEY_NAME, name);
        }
    }
    private void setNameForRoute(final GTFSObjectRoute route, final OSMEntity routeEntity) {
        final String name = route.getField(GTFSObjectRoute.FIELD_ROUTE_LONG_NAME);

        if(name != null && !name.isEmpty()) {
            routeEntity.setTag(OSMEntity.KEY_NAME, name);
        } else {
            final String ref = routeEntity.getTag(OSMEntity.KEY_REF);
            if(ref == null) {
                routeEntity.setTag(OSMEntity.KEY_NAME, "UNKNOWN ROUTE NUMBER");
            } else {
                routeEntity.setTag(OSMEntity.KEY_NAME, "Route " + ref);
            }
        }
    }

    /**
     * Tracks the GTFS entities that have been added to the com.company.meanfreepathllc.OSM entity space, to avoid duplication
     */
    private final HashMap<Integer, OSMEntity> allGTFSEntities = new HashMap<>(INITIAL_CAPACITY);
    private final OSMEntitySpace outputEntitySpace;

    public DataTransmutator(final String datasetId, final String datasetSource) {
        this.datasetId = datasetId;
        this.datasetSource = datasetSource;
        outputEntitySpace = new OSMEntitySpace(INITIAL_CAPACITY);
    }

    private OSMWay transmuteGTFSTrip(final GTFSObjectTrip tripData, final OSMEntity tripEntity, final OSMEntity routeEntity, final String tripGroupingField) {
        final OSMWay shapeWay = outputEntitySpace.createWay(null, null);
      //  System.out.println("Trip " + tripData.getField(GTFSObjectTrip.FIELD_TRIP_ID) + ": " + tripData.fields.toString());
        String tripRef = routeEntity.getTag(OSMEntity.KEY_REF);
        if(tripRef == null) {
            tripRef = tripData.getField(GTFSObjectTrip.FIELD_TRIP_ID);
        }

        shapeWay.setTag(KEY_GTFS_DATASET_ID, datasetId);
        shapeWay.setTag(OSMEntity.KEY_SOURCE, datasetSource);
        shapeWay.setTag(KEY_GTFS_SHAPE_ID, tripData.getField(GTFSObjectTrip.FIELD_SHAPE_ID));
        shapeWay.setTag(KEY_GTFS_TRIP_MARKER, generateTripMarker(tripData, tripGroupingField));
        shapeWay.setTag(OSMEntity.KEY_REF, routeEntity.getTag(OSMEntity.KEY_REF) + ":" + tripRef);
        shapeWay.setTag(OSMEntity.KEY_NAME, tripEntity.getTag(OSMEntity.KEY_NAME));

        for (final GTFSObjectShape.ShapePoint pt : tripData.shape.points) {
            shapeWay.appendNode(outputEntitySpace.createNode(pt.latitude, pt.longitude, null));
        }

        outputEntitySpace.addEntity(shapeWay, OSMEntity.TagMergeStrategy.keepTags, null);
        return shapeWay;
    }
    private static String generateTripMarker(final GTFSObjectTrip trip, final String tripGroupingField) {
        final String tripIdentifierFormat = "%s:%s";
        return String.format(tripIdentifierFormat, trip.getField(tripGroupingField), trip.getField(GTFSObjectTrip.FIELD_DIRECTION_ID));
    }
    public OSMRelation transmuteGTFSRoute(final GTFSObjectRoute routeData, final String tripGroupingField) throws IllegalArgumentException {
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
        setRefForRoute(routeData, osmRouteMaster);

        //route_long_name
        setNameForRoute(routeData, osmRouteMaster);

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
        final List<GTFSObjectTrip> tripsToUse = new ArrayList<>(8);
        condenseSubRoutes(routeData.trips, tripGroupingField, tripsToUse);

        //and add as members to the relation
        OSMRelation tripRoute;
        OSMWay shapeWay;
        final String tripIdentifierFormat = "%s:%s";
        for(GTFSObjectTrip trip: tripsToUse) {
            //init the OSM route relation for the trip and set the tags as needed
            tripRoute = outputEntitySpace.createRelation(null, null);
            OSMPresetFactory.makeRoute(tripRoute);
            tripRoute.setTag(KEY_GTFS_DATASET_ID, datasetId);
            tripRoute.setTag(OSMEntity.KEY_SOURCE, datasetSource);
            tripRoute.setTag(KEY_GTFS_TRIP_MARKER, generateTripMarker(trip, tripGroupingField));
            tripRoute.setTag(KEY_GTFS_AGENCY_ID, routeData.agency.getField(GTFSObjectAgency.FIELD_AGENCY_ID));
            tripRoute.setTag(KEY_GTFS_TRIP_ID, trip.getField(GTFSObjectTrip.FIELD_TRIP_ID));
            //tripRoute.setTag(KEY_GTFS_SHAPE_ID, trip.getField(GTFSObjectTrip.FIELD_SHAPE_ID)); //debug only
            setNameForTrip(routeData, trip, tripRoute);
            setRefForTrip(routeData, trip, tripRoute);
            tripRoute.setTag(OSMEntity.KEY_ROUTE, osmRouteMaster.getTag(OSMEntity.KEY_ROUTE_MASTER));
            tripRoute.setTag(OSMEntity.KEY_OPERATOR, osmRouteMaster.getTag(OSMEntity.KEY_OPERATOR));

            //add the shape (which details the path of the route) for later application to OSM highways
            shapeWay = transmuteGTFSTrip(trip, tripRoute, osmRouteMaster, tripGroupingField);
            shapeWay.setTag(routeWayTags[0], routeWayTags[1]);
            tripRoute.addMember(shapeWay, OSMEntity.MEMBERSHIP_DEFAULT);

            //also compile the stops and add them to the relation
            for(GTFSObjectTrip.StopTime stopTime: trip.stops) {
                processStopForRoute(stopTime.stop, routeData, tripRoute);
            }

            //and add the trip to the route master
            osmRouteMaster.addMember(tripRoute, OSMEntity.MEMBERSHIP_DEFAULT);
        }
        //System.out.format("%d/%d/%d=%d entities in space", outputEntitySpace.allNodes.size(), outputEntitySpace.allWays.size(), outputEntitySpace.allRelations.size(), outputEntitySpace.allEntities.size());
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

    /**
     * Condense the list of subroutes down, based on the stops they hit.  If a subroute's stops are an ordered subset
     * of any other subroute, disregard the former subroute
     * @param allTrips
     */
    public void condenseSubRoutes(final List<GTFSObjectTrip> allTrips, final String tripGroupingField, final List<GTFSObjectTrip> uniqueTrips) {
        //first group the trips by their direction field, and the given grouping field (i.e. shape id), if any
        final Map<Integer, Collection<GTFSObjectTrip>> uniqueTripsByDirection = new HashMap<>(2);
        if(tripGroupingField != null) {
            final HashMap<Integer, Map<String, GTFSObjectTrip>> tripGrouping = new HashMap<>(2);
            GTFSObjectTrip maxGroupTrip;
            for (GTFSObjectTrip trip : allTrips) {
                if (trip.shape != null) {
                    Map<String, GTFSObjectTrip> tripsForDirection = tripGrouping.get(trip.direction.ordinal());
                    if(tripsForDirection == null) {
                        tripsForDirection = new HashMap<>(4);
                        tripGrouping.put(trip.direction.ordinal(), tripsForDirection);
                    }

                    //groupingId = String.format(groupingFormat, trip.direction.ordinal(), trip.getField(tripGroupingField));
                    maxGroupTrip = tripsForDirection.get(trip.getField(tripGroupingField));
                    if (maxGroupTrip == null || trip.shape.totalDistanceTraveled > maxGroupTrip.shape.totalDistanceTraveled) {
                        tripsForDirection.put(trip.getField(tripGroupingField), trip);
                    }
                }
            }

            for(final Map.Entry<Integer, Map<String, GTFSObjectTrip>> tripsForDirection : tripGrouping.entrySet()) {
                uniqueTripsByDirection.put(tripsForDirection.getKey(), tripsForDirection.getValue().values());
            }
        } else { //otherwise, just group by trip direction
            for(final GTFSObjectTrip trip : allTrips) {
                Collection<GTFSObjectTrip> tripsForDirection = uniqueTripsByDirection.get(trip.direction.ordinal());
                if(tripsForDirection == null) {
                    tripsForDirection = new ArrayList<>(4);
                    uniqueTripsByDirection.put(trip.direction.ordinal(), tripsForDirection);
                }
                tripsForDirection.add(trip);
            }
        }


        //Now add the subroutes to the importRoutes list - these are the subroutes that will be processed for matches
        final Map<GTFSObjectTrip, List<String>> routeStopIds = new HashMap<>(uniqueTripsByDirection.size());
        for(final Collection<GTFSObjectTrip> tripsByDirection : uniqueTripsByDirection.values()) {
            //create an ordered list of the stops in the subroute
            for(final GTFSObjectTrip trip : tripsByDirection) {
                final ArrayList<String> stopIds = new ArrayList<>(trip.stops.size());
                for (final GTFSObjectTrip.StopTime stop : trip.stops) {
                    stopIds.add(stop.stop.getField(GTFSObjectStop.FIELD_STOP_ID));
                }
                routeStopIds.put(trip, stopIds);
            }
        }

        //now create a list of the subroutes whose stops are the same (or are an ordered subset of) another subroute
        final Map<GTFSObjectTrip, List<String>> stopIdDuplicates = new HashMap<>(routeStopIds.size());
        int i = 0, j;
        for(final Map.Entry<GTFSObjectTrip, List<String>> stopIds : routeStopIds.entrySet()) {
            j = 0;
            if(stopIdDuplicates.containsKey(stopIds.getKey())) { //don't process if already marked as a subset/dupe of other
                //System.out.println(i + ":" + j + "::" + stopIds.getKey().osm_id + " already a duplicate OUTER");
                i++;
                continue;
            }
            for(final Map.Entry<GTFSObjectTrip, List<String>> otherStopIds : routeStopIds.entrySet()) {
                if(stopIds.getValue() == otherStopIds.getValue()) { //don't compare equals!
                    //System.out.println(i + ":" + j++ + " IS SAME");
                    continue;
                }
                if(stopIdDuplicates.containsKey(otherStopIds.getKey())) { //don't process if already marked as a subset/dupe of other
                    //System.out.println(i + ":" + j++ + "::" + stopIds.getKey().osm_id + " already a duplicate INNER");
                    continue;
                }

                //if the stops in both sets are equal and in order, mark one of them as a duplicate
                if(stopIds.getValue().equals(otherStopIds.getValue())) {
                    //System.out.println(i + ":" + j++ + "::" + stopIds.getKey().osm_id + " is EQUAL to " + otherStopIds.getKey().osm_id);
                    stopIdDuplicates.put(otherStopIds.getKey(), otherStopIds.getValue());
                    continue;
                }

                //check if the otherStopIds array fully contains stopIds, including stop order
                final List<String> mostStops, leastStops;
                if(stopIds.getValue().size() > otherStopIds.getValue().size()) {
                    mostStops = stopIds.getValue();
                    leastStops = otherStopIds.getValue();
                } else {
                    mostStops = otherStopIds.getValue();
                    leastStops = stopIds.getValue();
                }
                //initial containsAll() check (unordered)
                if(mostStops.containsAll(leastStops)) {
                    //also do an order check, by finding the common elements with retainAll() and comparing
                    final List<String> commonElements = new ArrayList<>(mostStops);
                    commonElements.retainAll(leastStops);

                    //if the common elements .equals() the smaller array, it's an ordered subset and can be de-duped
                    if(commonElements.equals(leastStops)) {
                        if(mostStops == stopIds.getValue()) {
                            stopIdDuplicates.put(otherStopIds.getKey(), otherStopIds.getValue());
                            //System.out.println(i + ":" + j + "::" + otherStopIds.getKey().osm_id + " is DUPLICATE OF" + stopIds.getKey().osm_id);
                        } else if (mostStops == otherStopIds.getValue()){
                            stopIdDuplicates.put(stopIds.getKey(), stopIds.getValue());
                            //System.out.println(i + ":" + j + "::" + stopIds.getKey().osm_id + " is DUPLICATE OF" + otherStopIds.getKey().osm_id);
                            break; //bail since the outer item is no longer to be used
                        }
                    }
                }
                j++;
            }
            i++;
        }

        //now add the de-duplicated subroutes to the list to be processed
        for(final Map.Entry<GTFSObjectTrip, List<String>> routeStops : routeStopIds.entrySet()) {
            if(stopIdDuplicates.containsKey(routeStops.getKey())) {
                // System.out.println("DUPED " + routeStops.getKey().osm_id + ": " + String.join(":", routeStops.getValue()));
                continue;
            }
            //System.out.println("USING " + routeStops.getKey().osm_id + ": " + String.join(":", routeStops.getValue()));
            uniqueTrips.add(routeStops.getKey());
        }
    }
}
