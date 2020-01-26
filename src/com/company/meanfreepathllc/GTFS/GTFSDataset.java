package com.company.meanfreepathllc.GTFS;

import com.company.meanfreepathllc.OSM.Point;

import java.util.*;

/**
 * Manages all the data for a particular GTFS dataset
 * Created by nick on 4/5/17.
 */
public class GTFSDataset {
    public enum AgencyIdStatus {
        Unknown, Present, NotPresent
    }
    public AgencyIdStatus agencyIdStatus = AgencyIdStatus.Unknown;
    public final String basePath;

    public final List<GTFSObjectAgency> allAgencies = new ArrayList<>(GTFSObjectAgency.INITIAL_CAPACITY);
    public final HashMap<String, GTFSObjectAgency> agencyLookup = new HashMap<>(GTFSObjectAgency.INITIAL_CAPACITY);

    public final List<GTFSObjectCalendar> allCalendars = new ArrayList<>(GTFSObjectCalendar.INITIAL_CAPACITY);
    public final HashMap<String, GTFSObjectCalendar> calendarLookup = new HashMap<>(GTFSObjectCalendar.INITIAL_CAPACITY);

    public final List<GTFSObjectRoute> allRoutes = new ArrayList<>(GTFSObjectRoute.INITIAL_CAPACITY);
    public final HashMap<String, GTFSObjectRoute> routeLookup = new HashMap<>(GTFSObjectRoute.INITIAL_CAPACITY);

    public final List<GTFSObjectTrip> allTrips = new ArrayList<>(GTFSObjectTrip.INITIAL_CAPACITY);
    public final HashMap<String, GTFSObjectTrip> tripLookup = new HashMap<>(GTFSObjectTrip.INITIAL_CAPACITY);

    public final List<GTFSObjectShape> allShapes = new ArrayList<>(GTFSObjectShape.INITIAL_CAPACITY);
    public final HashMap<String, GTFSObjectShape> shapeLookup = new HashMap<>(GTFSObjectShape.INITIAL_CAPACITY);

    public final HashMap<String, GTFSObjectStop> stopLookup = new HashMap<>(GTFSObjectStop.INITIAL_CAPACITY);

    public final List<GTFSObjectStopTime> allStopTimes = new ArrayList<>(GTFSObjectStopTime.INITIAL_CAPACITY);

    public GTFSDataset(String path) {
        basePath = path;
    }

    public static void compareDatasets(GTFSDataset dataset1, GTFSDataset dataset2) {
        //routes
        Collection<String> routesOld = dataset1.routeLookup.keySet();
        Collection<String> routesNew = dataset2.routeLookup.keySet();
        List<String> routesAdded = new ArrayList<>(routesNew);
        routesAdded.removeAll(routesOld);
        List<String> routesRemoved = new ArrayList<>(routesOld);
        routesRemoved.removeAll(routesNew);

        System.out.println("Routes Added");
        System.out.println(routesAdded);
        System.out.println("Routes Removed");
        System.out.println(routesRemoved);

        List<String> commonRoutes = new ArrayList<>(routesOld);
        commonRoutes.removeAll(routesRemoved);
        for(final String routeId : commonRoutes) {
            GTFSObjectRoute routeOld = dataset1.routeLookup.get(routeId);
            GTFSObjectRoute routeNew = dataset2.routeLookup.get(routeId);

            Map<String, GTFSObjectStop> routeOldStops = new HashMap<>(64), routeNewStops = new HashMap<>(64);
            Map<String, GTFSObjectTrip> tripsNewLookup = new HashMap<>(routeNew.trips.size()), tripsOldLookup = new HashMap<>(routeOld.trips.size());
            for(final GTFSObjectTrip trip : routeNew.trips) {
                tripsNewLookup.put(trip.getField(GTFSObjectTrip.FIELD_TRIP_ID), trip);
                for(GTFSObjectTrip.StopTime stopInstance : trip.stops) {
                    routeNewStops.put(stopInstance.stop.getField(GTFSObjectStop.FIELD_STOP_ID), stopInstance.stop);
                }
            }
            for(final GTFSObjectTrip trip : routeOld.trips) {
                tripsOldLookup.put(trip.getField(GTFSObjectTrip.FIELD_TRIP_ID), trip);
                for(GTFSObjectTrip.StopTime stopInstance : trip.stops) {
                    routeOldStops.put(stopInstance.stop.getField(GTFSObjectStop.FIELD_STOP_ID), stopInstance.stop);
                }
            }

            Map<String, GTFSObjectTrip> tripsAdded = new HashMap<>(tripsNewLookup);
            tripsAdded.keySet().removeIf(tripsOldLookup::containsKey);
            Map<String, GTFSObjectTrip> tripsRemoved = new HashMap<>(tripsOldLookup);
            tripsRemoved.keySet().removeIf(tripsNewLookup::containsKey);


            Map<String, GTFSObjectStop> stopsAdded = new HashMap<>(routeNewStops);
            stopsAdded.keySet().removeIf(routeOldStops::containsKey);
            Map<String, GTFSObjectStop> stopsRemoved = new HashMap<>(routeOldStops);
            stopsRemoved.keySet().removeIf(routeNewStops::containsKey);

            System.out.format("Route %s (id %s): added %d trips, removed %d.  %d stops added, %d removed.\n", routeOld.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME), routeId, tripsAdded.size(), tripsRemoved.size(), stopsAdded.size(), stopsRemoved.size());
            for(final GTFSObjectStop stop : stopsAdded.values()) {
                System.out.format("\tADDED %s: %s\n", stop.getField(GTFSObjectStop.FIELD_STOP_ID), stop.getField(GTFSObjectStop.FIELD_STOP_NAME));
            }
            for(final GTFSObjectStop stop : stopsRemoved.values()) {
                System.out.format("\tREMOVED %s: %s\n", stop.getField(GTFSObjectStop.FIELD_STOP_ID), stop.getField(GTFSObjectStop.FIELD_STOP_NAME));
            }
        }

        //stops
        Map<String, GTFSObjectStop> stopsAdded = new HashMap<>(dataset2.stopLookup);
        stopsAdded.keySet().removeIf(dataset1.stopLookup::containsKey);
        Map<String, GTFSObjectStop> stopsRemoved = new HashMap<>(dataset1.stopLookup);
        stopsRemoved.keySet().removeIf(dataset2.stopLookup::containsKey);
        System.out.format("%d Stops ADDED:\n", stopsAdded.size());
        for(final GTFSObjectStop addStop : stopsAdded.values()) {
            System.out.format("\tStop %s (%s) ADDED\n", addStop.getField(GTFSObjectStop.FIELD_STOP_ID), addStop.getField(GTFSObjectStop.FIELD_STOP_NAME));
        }
        System.out.format("%d Stops DELETED:\n", stopsRemoved.size());
        for(final GTFSObjectStop delStop : stopsAdded.values()) {
            System.out.format("\tStop %s (%s) DELETED\n", delStop.getField(GTFSObjectStop.FIELD_STOP_ID), delStop.getField(GTFSObjectStop.FIELD_STOP_NAME));
        }

        //check if any stops moved
        Map<String, GTFSObjectStop> commonStops = new HashMap<>(dataset1.stopLookup);
        commonStops.keySet().removeIf(stopsRemoved::containsKey);
        System.out.format("%d added, %d removed, %d common\n", stopsAdded.size(), stopsRemoved.size(), commonStops.size());
        for(final String stopId : commonStops.keySet()) {
            GTFSObjectStop oldStop = dataset1.stopLookup.get(stopId), newStop = dataset2.stopLookup.get(stopId);
            double distance = Point.distance(oldStop.coordinate, newStop.coordinate);
            if(distance >= 3.0) {
                System.out.format("\tStop %s (%s) moved %.01f meters\n", oldStop.getField(GTFSObjectStop.FIELD_STOP_ID), oldStop.getField(GTFSObjectStop.FIELD_STOP_NAME), distance);
            }
        }
    }

    GTFSObjectAgency lookupAgencyById(String id) {
        switch (agencyIdStatus) {
            case Unknown:
                return null;
            case Present:
                return agencyLookup.get(id);
            case NotPresent:
                if(allAgencies.size() > 0) {
                    return allAgencies.get(0);
                } else {
                    return null;
                }
        }
        return null;
    }
    void addAgency(GTFSObjectAgency agency) {
        allAgencies.add(agency);

        //flag whether the agency ID is present
        String agencyId = agency.getField(GTFSObjectAgency.FIELD_AGENCY_ID);
        if(agencyIdStatus == AgencyIdStatus.Unknown) {
            agencyIdStatus = agencyId != null ? AgencyIdStatus.Present : AgencyIdStatus.NotPresent;
        }

        //only add to the lookup if IDs are present
        if(agencyIdStatus == AgencyIdStatus.Present) {
            agencyLookup.put(agencyId, agency);
        }
    }
}
