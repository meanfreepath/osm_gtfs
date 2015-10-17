package com.company.meanfreepathllc.GTFS;

import com.company.meanfreepathllc.OSM.OSMBusPlatform;
import com.company.meanfreepathllc.OSM.OSMEntity;
import com.company.meanfreepathllc.OSM.OSMNode;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nick on 10/15/15.
 */
public class GTFSStopsProcessor extends GTFSProcessor {
    private final static int INITIAL_STOP_CAPACITY = 8192;
    private final static String FILE_NAME = "stops.txt";

    private final static String[] GTFS_COLUMNS = {"stop_id","stop_name","stop_lat","stop_lon","zone_id","stop_timezone"};
    private ArrayList<OSMNode> stops;

    private OSMBusPlatform curStop;
    private String curGTFSColumnValue;

    private final static String BAY_REGEX = "[ :-]* bay ([\\S\\d]+)";
    private Pattern bayPattern = Pattern.compile(BAY_REGEX, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);

    public GTFSStopsProcessor() throws IOException {
        gtfsColumnIndices = new HashMap<>(GTFS_COLUMNS.length);
        initGTFSColumnHandler();

        fp = new File(basePath + FILE_NAME);
        if(!fp.exists()) {
            throw new FileNotFoundException("No stops.txt file found in directory!");
        }

        stops = new ArrayList<OSMNode>(INITIAL_STOP_CAPACITY);
        List<String> explodedLine;
        int lineNumber = 0;
        short colIdx;

        FileInputStream fStream = new FileInputStream(fp.getAbsoluteFile());
        BufferedReader in = new BufferedReader(new InputStreamReader(fStream));
        while (in.ready()) {
            explodedLine = parseLine(in);

            if(explodedLine == null) { //i.e. blank line
                continue;
            }
            if(lineNumber++ == 0) { //header line
                colIdx = 0;
                for (String colVal: explodedLine) {
                    gtfsColumnIndices.put(new Short(colIdx++), colVal);
                }
                continue;
            }

            curStop = OSMBusPlatform.create();
            colIdx = 0;
            for (String colVal : explodedLine) {
                curGTFSColumnValue = colVal;
                gtfsFieldHandlers.get(gtfsColumnIndices.get(colIdx++)).run();
            }
            stops.add(curStop);
        }
        in.close();
        fStream.close();
    }
    public ArrayList<OSMNode> getStops() {
        return stops;
    }

    @Override
    protected void initGTFSColumnHandler() {
        gtfsFieldHandlers = new HashMap<String,Runnable>(GTFS_COLUMNS.length);
        //{"stop_id","stop_name","stop_lat","stop_lon","zone_id","stop_timezone"}
        gtfsFieldHandlers.put("stop_id", new Runnable() {
            @Override
            public void run() {
                processStopId(curGTFSColumnValue, curStop);
            }
        });
        gtfsFieldHandlers.put("stop_name", new Runnable() {
            @Override
            public void run() {
                processStopName(curGTFSColumnValue, curStop);
            }
        });
        gtfsFieldHandlers.put("stop_lat", new Runnable() {
            @Override
            public void run() {
                processStopLat(curGTFSColumnValue, curStop);
            }
        });
        gtfsFieldHandlers.put("stop_lon", new Runnable() {
            @Override
            public void run() {
                processStopLon(curGTFSColumnValue, curStop);
            }
        });
        gtfsFieldHandlers.put("zone_id", new Runnable() {
            @Override
            public void run() {
                processZoneId(curGTFSColumnValue, curStop);
            }
        });
        gtfsFieldHandlers.put("stop_timezone", new Runnable() {
            @Override
            public void run() {
                processStopTimezone(curGTFSColumnValue, curStop);
            }
        });
    }
    private void processStopId(String value, OSMNode route) {
        route.setTag(OSMEntity.KEY_REF, value);
    }
    private void processStopName(String value, OSMNode route) {
        //Extract the bay from the name, if any, and put in the local_ref tag
        Matcher m = bayPattern.matcher(value);
        if(m.find()) {
            route.setTag(OSMEntity.KEY_LOCAL_REF, m.group(1));
            value = m.replaceAll("");
        }
        route.setTag(OSMEntity.KEY_NAME, value);
    }
    private void processStopLat(String value, OSMNode route) {
        route.setTag(OSMEntity.KEY_LATITUDE, value);
    }
    private void processStopLon(String value, OSMNode route) {
        route.setTag(OSMEntity.KEY_LONGITUDE, value);
    }
    private void processZoneId(String value, OSMNode route) {
        //route.setTag(OSMEntity.KEY_WEBSITE, value);
    }
    private void processStopTimezone(String value, OSMNode route) {
        //Currently ignored
    }
}
