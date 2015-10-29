package com.company.meanfreepathllc;

import com.company.meanfreepathllc.GTFS.*;
import com.company.meanfreepathllc.OSM.OSMEntity;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
	// write your code here
        GTFSProcessor.setBasePath("/Users/nick/Downloads/1_gtfs/");

        try {
            System.out.print("Processing agencies...\n");
            GTFSProcessorAgency agencyProcessor = GTFSProcessorAgency.initInstance();
            System.out.printf("%d agencies found\n", GTFSObjectAgency.allAgencies.size());

            System.out.print("Processing calendar...\n");
            GTFSProcessorCalendar calendarProcessor = GTFSProcessorCalendar.initInstance();
            System.out.printf("%d calendars found\n", GTFSObjectCalendar.allCalendars.size());

            System.out.print("Processing shapes...\n");
            GTFSProcessorShapes shapeProcessor = GTFSProcessorShapes.initInstance();
            System.out.printf("%d shapes found\n", GTFSObjectShape.allShapes.size());

            System.out.print("Processing routes...\n");
            GTFSProcessorRoute routeProcessor = new GTFSProcessorRoute();
            System.out.printf("%d routes found\n", GTFSObjectRoute.allRoutes.size());

            System.out.print("Processing stops...\n");
            GTFSProcessorStops stopsProcessor = new GTFSProcessorStops();
            System.out.printf("%d stops found\n", GTFSObjectStop.allStops.size());

            System.out.print("Processing trips...\n");
            GTFSProcessorTrips tripsProcessor = new GTFSProcessorTrips();
            System.out.printf("%d trips found\n", GTFSObjectTrip.allTrips.size());

            System.out.print("Processing stop times...\n");
            GTFSProcessorStopTimes stopTimesProcessor = new GTFSProcessorStopTimes();
            System.out.printf("%d stop times found\n", GTFSObjectStopTime.allStopTimes.size());

            for(GTFSObjectTrip trip: GTFSObjectTrip.allTrips) {
            //    System.out.printf("Trip %s has %d stops\n", trip.getField(GTFSObjectTrip.FIELD_TRIP_ID), trip.stops.size());
            }
            for(GTFSObjectRoute route: GTFSObjectRoute.allRoutes) {
                System.out.printf("Route %s has %d trips\n", route.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME), route.trips.size());
            }

            final String s = OSMEntity.outputXml(stopsProcessor.getStops());
           // final String s = OSMEntity.outputXml(routeProcessor.getRoutes());
            FileWriter fp = new FileWriter(GTFSProcessor.getBasePath() + "/stops.osm");
            fp.write(s);
            fp.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
