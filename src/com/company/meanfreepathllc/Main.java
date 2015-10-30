package com.company.meanfreepathllc;

import com.company.meanfreepathllc.GTFS.*;
import com.company.meanfreepathllc.OSM.OSMEntity;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InvalidArgumentException {

        GTFSProcessor.setBasePath("/Users/nick/Downloads/1_gtfs/");

        try {
            System.out.print("Processing agencies...\n");
            GTFSProcessor.processData(GTFSObjectAgency.class);
            System.out.printf("%d agencies found\n", GTFSObjectAgency.allAgencies.size());

            System.out.print("Processing calendar...\n");
            GTFSProcessor.processData(GTFSObjectCalendar.class);
            System.out.printf("%d calendars found\n", GTFSObjectCalendar.allCalendars.size());

            System.out.print("Processing shapes...\n");
            GTFSProcessor.processData(GTFSObjectShape.class);
            System.out.printf("%d shapes found\n", GTFSObjectShape.allShapes.size());

            System.out.print("Processing routes...\n");
            GTFSProcessor.processData(GTFSObjectRoute.class);
            System.out.printf("%d routes found\n", GTFSObjectRoute.allRoutes.size());

            System.out.print("Processing stops...\n");
            GTFSProcessor.processData(GTFSObjectStop.class);
            System.out.printf("%d stops found\n", GTFSObjectStop.allStops.size());

            System.out.print("Processing trips...\n");
            GTFSProcessor.processData(GTFSObjectTrip.class);
            System.out.printf("%d trips found\n", GTFSObjectTrip.allTrips.size());

            System.out.print("Processing stop times...\n");
            GTFSProcessor.processData(GTFSObjectStopTime.class);
            System.out.printf("%d stop times found\n", GTFSObjectStopTime.allStopTimes.size());

            List<OSMEntity> wayList = new ArrayList<>();
            for(GTFSObjectRoute route: GTFSObjectRoute.allRoutes) {
                if(route.getField(GTFSObjectRoute.FIELD_ROUTE_ID).equals("100479")) {
                    wayList.add(DataTransmutator.transmuteGTFSRoute(route));
                }
                System.out.printf("Route %s has %d trips\n", route.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME), route.trips.size());
            }
            System.out.println("Writing to file…");
            OSMEntity.outputXml(wayList, GTFSProcessor.getBasePath() + "/routes.osm");

            System.out.println("Finished!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        //} catch (InvalidArgumentException e) {
          //  e.printStackTrace();
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
