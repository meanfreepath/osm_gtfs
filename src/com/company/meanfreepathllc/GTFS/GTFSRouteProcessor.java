package com.company.meanfreepathllc.GTFS;

import com.company.meanfreepathllc.OSM.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/16/15.
 */
public class GTFSRouteProcessor extends GTFSProcessor {
    private final static int INITIAL_ROUTE_CAPACITY = 256;
    private final static String FILE_NAME = "routes.txt";

    private static HashMap<String,Runnable> gtfsColumns2;
    private final static String[] GTFS_COLUMNS = {"agency_id", "route_id", "route_short_name", "route_long_name", "route_type", "route_desc", "route_url"};
    private final ArrayList<OSMRoute> routes;

    private String curGTFSColumnValue;
    private OSMRoute curRoute;

    private class ColumnHandler implements Runnable {
        public final String colName;

        public ColumnHandler(String col) {
            colName = col;
        }

        @Override
        public void run() {
            switch (colName) {
                //case GTFS_COLUMNS[0]
            }
        }
    }
    public GTFSRouteProcessor() throws IOException, FileNotFoundException {
        gtfsColumnIndices = new HashMap<>(GTFS_COLUMNS.length);
        initGTFSColumnHandler();

        fp = new File(basePath + FILE_NAME);
        if(!fp.exists()) {
            throw new FileNotFoundException("No routes.txt file found in directory!");
        }

        routes = new ArrayList<OSMRoute>(INITIAL_ROUTE_CAPACITY);
        List<String> explodedLine;
        //OSMRoute curRoute; //TODO base route class on operator
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
                for (final String colVal: explodedLine) {
                    gtfsColumnIndices.put(new Short(colIdx++), colVal);
                }
                continue;
            }
            curRoute = OSMRouteBus.create();

            colIdx = 0;
            for (final String colVal : explodedLine) {
                curGTFSColumnValue = colVal;
                gtfsFieldHandlers.get(gtfsColumnIndices.get(colIdx++)).run();
            }
            routes.add(curRoute);
        }
        in.close();
        fStream.close();
    }
    protected void initGTFSColumnHandler() {
        gtfsFieldHandlers = new HashMap<String,Runnable>(GTFS_COLUMNS.length);
        gtfsFieldHandlers.put("agency_id", new Runnable() {
            @Override
            public void run() {
                processAgencyId(curGTFSColumnValue, curRoute);
            }
        });
        gtfsFieldHandlers.put("route_id", new Runnable() {
            @Override
            public void run() {
                processRouteId(curGTFSColumnValue, curRoute);
            }
        });
        gtfsFieldHandlers.put("route_short_name", new Runnable() {
            @Override
            public void run() {
                processRouteShortName(curGTFSColumnValue, curRoute);
            }
        });
        gtfsFieldHandlers.put("route_long_name", new Runnable() {
            @Override
            public void run() {
                processRouteLongName(curGTFSColumnValue, curRoute);
            }
        });
        gtfsFieldHandlers.put("route_type", new Runnable() {
            @Override
            public void run() {
                processRouteType(curGTFSColumnValue, curRoute);
            }
        });
        gtfsFieldHandlers.put("route_desc", new Runnable() {
            @Override
            public void run() {
                processRouteDesc(curGTFSColumnValue, curRoute);
            }
        });
        gtfsFieldHandlers.put("route_url", new Runnable() {
            @Override
            public void run() {
                processRouteUrl(curGTFSColumnValue, curRoute);
            }
        });
    }
    private void processAgencyId(String value, OSMRoute route) {
        GTFSAgencyProcessor.GTFSAgency routeAgency = GTFSAgencyProcessor.instance.agencyLookup.get(value);
        if(routeAgency == null) {
            logEvent(LogLevel.error, "No agency defined for agency_id \"" + value + "\" - this is an error with the GTFS dataset.");
            return;
        }
        route.setTag(OSMEntity.KEY_OPERATOR, routeAgency.name);
    }
    private void processRouteId(String value, OSMRoute route) {
        route.setTag(OSMEntity.KEY_REF, value);
    }
    private void processRouteShortName(String value, OSMRoute route) {
        if(value == null) {
            logEvent(LogLevel.warn, "No route_short_name present");
        } else {
            route.setTag(OSMEntity.KEY_REF, value);
        }
    }
    private void processRouteLongName(String value, OSMRoute route) {
        if(value != null && !value.isEmpty()) {
            route.setTag(OSMEntity.KEY_NAME, value);
        } else {
            String ref = route.getTag(OSMEntity.KEY_REF);
            if(ref == null) {
                route.setTag(OSMEntity.KEY_NAME, "UNKNOWN ROUTE NUMBER");
            } else {
                route.setTag(OSMEntity.KEY_NAME, "Route " + ref);
            }
        }
    }
    private void processRouteType(String value, OSMRoute route) {

    }
    private void processRouteDesc(String value, OSMRoute route) {
        final String[] splitters = {" to ", " - ", "/"};
        if(value == null) {
            return;
        } else {
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
                        for(short v=1;v<split.length - 1;v++) {
                            via.add(split[v]);
                        }
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
    private void processRouteUrl(String value, OSMRoute route) {
        route.setTag(OSMEntity.KEY_WEBSITE, value);
    }

    public ArrayList<OSMRoute> getRoutes() {
        return routes;
    }

}
