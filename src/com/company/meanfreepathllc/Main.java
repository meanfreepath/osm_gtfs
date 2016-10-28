package com.company.meanfreepathllc;

import com.company.meanfreepathllc.GTFS.*;
import com.company.meanfreepathllc.OSM.OSMEntity;
import com.company.meanfreepathllc.OSM.OSMEntitySpace;
import com.company.meanfreepathllc.OSM.OSMRelation;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InvalidArgumentException {

        //GTFSProcessor.setBasePath("/Users/nick/Downloads/GTFS_OSM/1_gtfs/");
        GTFSProcessor.setBasePath("/Users/nick/Downloads/GTFS_OSM/google_transit_2016/");
        GTFSProcessor.setBasePath("/Users/nick/Downloads/GTFS_OSM/trimet_gtfs/");
        //GTFSProcessor.setBasePath("/Users/nick/Downloads/GTFS_OSM/metro-los-angeles_20160110_0950/");
        //GTFSProcessor.setBasePath("/Users/nick/Downloads/GTFS_OSM/vancouver_gtfs/");
        //GTFSProcessor.setBasePath("/Users/nick/Downloads/GTFS_OSM/gtfs_puget_sound_consolidated/");

        final String datasetName = "KCGIS";
        final String datasetSource = "King County GIS";

        try {
            System.out.print("Processing agencies...\n");
            GTFSProcessor.processData(GTFSObjectAgency.class);
            System.out.printf("%d agencies found\n", GTFSObjectAgency.allAgencies.size());

            //check if the calendar.txt file is present - if not, then use calendar_dates.txt instead
            File calendarFile = new File(GTFSProcessor.getBasePath() + GTFSObjectCalendar.GTFS_FILE_NAME);
            if(calendarFile.exists()) {
                System.out.print("Processing calendars using calendar.txt...\n");
                GTFSProcessor.processData(GTFSObjectCalendar.class);
                System.out.printf("%d calendars found\n", GTFSObjectCalendar.allCalendars.size());
            } else {
                System.out.print("Processing calendars using calendar_dates.txt...\n");
                GTFSProcessor.processData(GTFSObjectCalendarDate.class);
                System.out.printf("%d calendars found\n", GTFSObjectCalendar.allCalendars.size());
            }
            calendarFile = null;

            System.out.print("Processing shapes...\n");
            GTFSProcessor.processData(GTFSObjectShape.class);
            System.out.printf("%d shapes found\n", GTFSObjectShape.shapeLookup.size());

            System.out.print("Processing routes...\n");
            GTFSProcessor.processData(GTFSObjectRoute.class);
            System.out.printf("%d routes found\n", GTFSObjectRoute.routeLookup.size());

            System.out.print("Processing stops...\n");
            GTFSProcessor.processData(GTFSObjectStop.class);
            System.out.printf("%d stops found\n", GTFSObjectStop.stopLookup.size());

            System.out.print("Processing trips...\n");
            GTFSProcessor.processData(GTFSObjectTrip.class);
            System.out.printf("%d trips found\n", GTFSObjectTrip.allTrips.size());

            //NOTE processing stop times isn't necessary for the purpose of OSM route imports
            System.out.print("Processing stop times...\n");
            GTFSProcessor.processData(GTFSObjectStopTime.class);
            System.out.printf("%d stop times found\n", GTFSObjectStopTime.allStopTimes.size());

            GTFSProcessor.outputEventLogs();


            //transmute the GTFS objects to OSM nodes/ways/relations
            final DataTransmutator export = new DataTransmutator(datasetName, datasetSource);
            final String debugRouteId = "";
            for(GTFSObjectRoute route: GTFSObjectRoute.allRoutes) {
                System.out.printf("Route id #%s: %s (%s) has %d trips...", route.getField(GTFSObjectRoute.FIELD_ROUTE_ID), route.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME), route.getField(GTFSObjectRoute.FIELD_ROUTE_LONG_NAME), route.trips.size());
                OSMRelation osmRoute = export.transmuteGTFSRoute(route, GTFSObjectTrip.FIELD_SHAPE_ID);
                if(route.getField(GTFSObjectRoute.FIELD_ROUTE_ID).equals(debugRouteId)) {
                    OSMEntitySpace space = new OSMEntitySpace(1024);
                    space.addEntity(osmRoute, OSMEntity.TagMergeStrategy.keepTags, null);
                    space.outputXml(GTFSProcessor.getBasePath() + "/routedebug.osm");
                    space = null;
                }
                System.out.printf("%d used\n", osmRoute.getMembers().size());
            }
            System.out.println("Writing to file…");
            export.outputToOSMXML(GTFSProcessor.getBasePath() + "/routes.osm");
            System.out.println("Finished!");

        } catch (FileNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        //} catch (InvalidArgumentException e) {
          //  e.printStackTrace();
            e.printStackTrace();
        }
    }
}
