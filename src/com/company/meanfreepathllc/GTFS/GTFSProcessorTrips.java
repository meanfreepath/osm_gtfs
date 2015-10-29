package com.company.meanfreepathllc.GTFS;

import com.company.meanfreepathllc.OSM.OSMNode;
import com.company.meanfreepathllc.OSM.OSMRoute;
import com.company.meanfreepathllc.OSM.OSMRouteBus;
import com.company.meanfreepathllc.OSM.OSMWay;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/28/15.
 */
public class GTFSProcessorTrips extends GTFSProcessor {
    public static GTFSProcessorTrips instance;

    public ArrayList<OSMWay> trips;

    static {
        FILE_NAME = "trips.txt";
    }

    public static GTFSProcessorTrips initInstance() throws IOException {
        instance = new GTFSProcessorTrips();
        return instance;
    }
    public GTFSProcessorTrips() throws IOException {
        gtfsColumnIndices = new HashMap<>();

        fp = new File(basePath + FILE_NAME);
        if(!fp.exists()) {
            throw new FileNotFoundException("No trips.txt file found in directory!");
        }

        trips = new ArrayList<>(GTFSObjectTrip.INITIAL_CAPACITY);
        GTFSObjectTrip tripData;
        List<String> explodedLine;
        int lineNumber = 0;

        FileInputStream fStream = new FileInputStream(fp.getAbsoluteFile());
        BufferedReader in = new BufferedReader(new InputStreamReader(fStream));
        while (in.ready()) {
            explodedLine = parseLine(in);
            if(explodedLine == null) { //i.e. blank line
                continue;
            }

            tripData = new GTFSObjectTrip();
            if(lineNumber++ == 0) { //header line
                processFileHeader(explodedLine, tripData);
                continue;
            }

            //run the handler for each column
            try {
                processLine(explodedLine, tripData);
            } catch (InvalidArgumentException e) {
                logEvent(LogLevel.error, e.getLocalizedMessage(), lineNumber+1);
                continue;
            }

            if(tripData.parentRoute == null) {
                logEvent(LogLevel.warn, "Missing route for trip id " + tripData.getField(GTFSObjectTrip.FIELD_TRIP_ID), lineNumber+1);
            }
            if(tripData.parentService == null) {
                logEvent(LogLevel.warn, "Missing service info for trip id " + tripData.getField(GTFSObjectTrip.FIELD_TRIP_ID), lineNumber+1);
            }

            OSMRoute curTrip = OSMRouteBus.create();
        }
        in.close();
        fStream.close();
    }
    public ArrayList<OSMRoute> getTrips() {
        return null;//trips;
    }
}
