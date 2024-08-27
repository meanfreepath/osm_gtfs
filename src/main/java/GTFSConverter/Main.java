package GTFSConverter;

import GTFSConverter.GTFS.*;
import GTFSConverter.OSM.OSMEntity;
import GTFSConverter.OSM.OSMEntitySpace;
import GTFSConverter.OSM.OSMRelation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Main {
    private enum ProcessingAction {
        process, compare
    }

    public static void main(String[] args) throws IllegalArgumentException {

        List<String> argList = new ArrayList<>(args.length);
        if(args.length <= 1) {
            System.err.println("Usage: java -jar GTFSConverter.jar -a " + Arrays.asList(ProcessingAction.values()) + " -f [path to GTFS directory]");
            System.exit(1);
        }

        Collections.addAll(argList, args);

        ProcessingAction processingAction = null;
        List<String> basePaths = null;
        ListIterator<String> argIterator = argList.listIterator();
        while(argIterator.hasNext()) {
            switch (argIterator.next()) {
                case "-f":
                    String paths[] = argIterator.next().split(",");
                    basePaths = new ArrayList<>(paths.length);
                    Collections.addAll(basePaths, paths);
                    break;
                case "-a":
                    String action = argIterator.next();
                    try {
                        processingAction = ProcessingAction.valueOf(action);
                    } catch (IllegalArgumentException err) {
                        System.err.println("ERROR: Invalid action - must be one of " + Arrays.asList(ProcessingAction.values()));
                        System.exit(1);
                    }
                    break;
                case "-h":
                    System.out.println("Usage: java -jar GTFSConverter.jar -a [action] -f [path to GTFS directory]");
                    System.exit(0);
                    break;
            }
        }
        if(processingAction == null) {
            System.err.println("ERROR: Need to specify an action");
            System.exit(1);
        }
        if(basePaths == null) {
            System.err.println("ERROR: Need at least 1 path to process");
            System.exit(1);
        }

        List<GTFSDataset> datasets = new ArrayList<>(basePaths.size());

        final String datasetName = "KCGIS";
        final String datasetSource = "King County GIS";

        for(final String path : basePaths) {
            GTFSDataset dataset = new GTFSDataset(path);
            datasets.add(dataset);
            System.out.println("Process dataset at path " + path + "…");
            try {
                System.out.print("Processing agencies...\n");
                GTFSProcessor.processData(GTFSObjectAgency.class, dataset);
                System.out.printf("%d agencies found\n", dataset.allAgencies.size());

                //check if the calendar.txt file is present - if not, then use calendar_dates.txt instead
                File calendarFile = new File(dataset.basePath + GTFSObjectCalendar.GTFS_FILE_NAME);
                if (calendarFile.exists()) {
                    System.out.print("Processing calendars using calendar.txt...\n");
                    GTFSProcessor.processData(GTFSObjectCalendar.class, dataset);
                    System.out.printf("%d calendars found\n", dataset.allCalendars.size());
                } else {
                    System.out.print("Processing calendars using calendar_dates.txt...\n");
                    GTFSProcessor.processData(GTFSObjectCalendarDate.class, dataset);
                    System.out.printf("%d calendars found\n", dataset.allCalendars.size());
                }
                calendarFile = null;

                System.out.print("Processing shapes...\n");
                GTFSProcessor.processData(GTFSObjectShape.class, dataset);
                System.out.printf("%d shapes found\n", dataset.shapeLookup.size());

                System.out.print("Processing routes...\n");
                GTFSProcessor.processData(GTFSObjectRoute.class, dataset);
                System.out.printf("%d routes found\n", dataset.routeLookup.size());

                System.out.print("Processing stops...\n");
                GTFSProcessor.processData(GTFSObjectStop.class, dataset);
                System.out.printf("%d stops found\n", dataset.stopLookup.size());

                System.out.print("Processing trips...\n");
                GTFSProcessor.processData(GTFSObjectTrip.class, dataset);
                System.out.printf("%d trips found\n", dataset.allTrips.size());

                //NOTE processing stop times isn't necessary for the purpose of com.company.meanfreepathllc.OSM route imports
                System.out.print("Processing stop times...\n");
                GTFSProcessor.processData(GTFSObjectStopTime.class, dataset);
                System.out.printf("%d stop times found\n", GTFSObjectStopTime.allStopTimes.size());

                GTFSProcessor.outputEventLogs();


                //transmute the GTFS objects to OSM nodes/ways/relations
                if(processingAction == ProcessingAction.process) {
                    final DataTransmutator export = new DataTransmutator(datasetName, datasetSource);
                    final String debugRouteId = "";
                    for (GTFSObjectRoute route : dataset.allRoutes) {
                        System.out.printf("Route id #%s: %s (%s) has %d trips...", route.getField(GTFSObjectRoute.FIELD_ROUTE_ID), route.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME), route.getField(GTFSObjectRoute.FIELD_ROUTE_LONG_NAME), route.trips.size());
                        OSMRelation osmRoute = export.transmuteGTFSRoute(route, GTFSObjectTrip.FIELD_SHAPE_ID);
                        if (route.getField(GTFSObjectRoute.FIELD_ROUTE_ID).equals(debugRouteId)) {
                            OSMEntitySpace space = new OSMEntitySpace(1024);
                            space.addEntity(osmRoute, OSMEntity.TagMergeStrategy.keepTags, null);
                            space.outputXml(dataset.basePath + "/routedebug.osm");
                            space = null;
                        }
                        System.out.printf("%d used\n", osmRoute.getMembers().size());
                    }
                    System.out.println("Writing to file…");
                    export.outputToOSMXML(dataset.basePath + "/routes.osm");
                    System.out.println("Finished!");
                }

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
}
