package com.company.meanfreepathllc.GTFS;

import com.company.meanfreepathllc.OSM.OSMBusPlatform;
import com.company.meanfreepathllc.OSM.OSMEntity;
import com.company.meanfreepathllc.OSM.OSMNode;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nick on 10/15/15.
 */
public class GTFSProcessorStops extends GTFSProcessor {
    private final static int INITIAL_STOP_CAPACITY = 8192;
    private final static String FILE_NAME = "stops.txt";

    private ArrayList<OSMNode> stops;

    private final static String BAY_REGEX = "[ :-]* bay ([\\S\\d]+)";
    private Pattern bayPattern = Pattern.compile(BAY_REGEX, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);

    public GTFSProcessorStops() throws IOException {

        fp = new File(basePath + FILE_NAME);
        if(!fp.exists()) {
            throw new FileNotFoundException("No stops.txt file found in directory!");
        }

        gtfsColumnIndices = new HashMap<>();
        stops = new ArrayList<OSMNode>(INITIAL_STOP_CAPACITY);
        GTFSObjectStop stopData;
        OSMBusPlatform curStop;

        List<String> explodedLine;
        int lineNumber = 0;
        FileInputStream fStream = new FileInputStream(fp.getAbsoluteFile());
        BufferedReader in = new BufferedReader(new InputStreamReader(fStream));
        while (in.ready()) {
            explodedLine = parseLine(in);

            if(explodedLine == null) { //i.e. blank line
                continue;
            }

            stopData = new GTFSObjectStop();

            if(lineNumber++ == 0) { //header line
                processFileHeader(explodedLine, stopData);
                continue;
            }

            try {
                processLine(explodedLine, stopData);
            } catch (InvalidArgumentException e) {
                logEvent(LogLevel.error, e.getMessage(), lineNumber+1);
                continue;
            }

            //now process into the OSM data
            curStop = OSMBusPlatform.create();

            //stop_id
            curStop.setTag(OSMEntity.KEY_REF, stopData.getField(GTFSObjectStop.FIELD_STOP_ID));

            //stop_name
            String stopName = stopData.getField(GTFSObjectStop.FIELD_STOP_NAME);
            Matcher m = bayPattern.matcher(stopName); //Extract the bay from the name, if any, and put in the local_ref tag
            if(m.find()) {
                curStop.setTag(OSMEntity.KEY_LOCAL_REF, m.group(1));
                stopName = m.replaceAll("");
            }
            curStop.setTag(OSMEntity.KEY_NAME, stopName);

            //stop_lat, stop_lon
            curStop.setTag(OSMEntity.KEY_LATITUDE, stopData.getField(GTFSObjectStop.FIELD_STOP_LAT));
            curStop.setTag(OSMEntity.KEY_LONGITUDE, stopData.getField(GTFSObjectStop.FIELD_STOP_LON));

            stops.add(curStop);
        }
        in.close();
        fStream.close();
    }
    public ArrayList<OSMNode> getStops() {
        return stops;
    }
}
