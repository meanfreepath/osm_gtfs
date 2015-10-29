package com.company.meanfreepathllc.GTFS;

import com.company.meanfreepathllc.GTFS.GTFSObjectRoute.GTFSRouteType;
import com.company.meanfreepathllc.OSM.*;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/16/15.
 */
public class GTFSProcessorRoute extends GTFSProcessor {
    private final static int INITIAL_ROUTE_CAPACITY = 256;
    private final static String FILE_NAME = "routes.txt";

    private final ArrayList<OSMRouteMaster> routes;

    public GTFSProcessorRoute() throws IOException, FileNotFoundException {
        super();
        gtfsColumnIndices = new HashMap<>(GTFSObjectRoute.requiredFields.length);

        fp = new File(basePath + FILE_NAME);
        if(!fp.exists()) {
            throw new FileNotFoundException("No routes.txt file found in directory!");
        }

        routes = new ArrayList<>(INITIAL_ROUTE_CAPACITY);
        List<String> explodedLine;
        OSMRouteMaster curRoute; //TODO base route class on operator
        int lineNumber = 0;
        GTFSObjectRoute routeData;

        final FileInputStream fStream = new FileInputStream(fp.getAbsoluteFile());
        final BufferedReader in = new BufferedReader(new InputStreamReader(fStream));
        while (in.ready()) {
            explodedLine = parseLine(in);
            if(explodedLine == null) { //i.e. blank line
                continue;
            }

            routeData = new GTFSObjectRoute();
            if(lineNumber++ == 0) { //header line
                processFileHeader(explodedLine, routeData);
                continue;
            }

            //compile the route data object's fields from the line's data
            try {
                processLine(explodedLine, routeData);
            } catch (InvalidArgumentException e) {
                logEvent(LogLevel.warn, e.getLocalizedMessage(), lineNumber+1);
                continue;
            }

            //now copy the GTFS route data as an OSM route_master relation
            curRoute = OSMRouteMaster.create();

            //agency_id
            if(routeData.agency == null) {
                logEvent(LogLevel.error, "No agency defined for agency_id \"" + routeData.getField(GTFSObjectRoute.FIELD_AGENCY_ID) + "\" - this is an error with the GTFS dataset.", lineNumber+1);
                return;
            }
            curRoute.setTag(OSMEntity.KEY_OPERATOR, routeData.agency.getField(GTFSObjectAgency.FIELD_AGENCY_NAME));

            //route_id
            curRoute.setTag("kcm:id", routeData.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME));

            //route_short_name
            curRoute.setTag(OSMEntity.KEY_REF, routeData.getField(GTFSObjectRoute.FIELD_ROUTE_SHORT_NAME));

            //route_long_name
            String routeLongName = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_LONG_NAME);
            if(routeLongName != null && !routeLongName.isEmpty()) {
                curRoute.setTag(OSMEntity.KEY_NAME, routeLongName);
            } else {
                String ref = curRoute.getTag(OSMEntity.KEY_REF);
                if(ref == null) {
                    curRoute.setTag(OSMEntity.KEY_NAME, "UNKNOWN ROUTE NUMBER");
                } else {
                    curRoute.setTag(OSMEntity.KEY_NAME, "Route " + ref);
                }
            }

            //route_desc
            processRouteDesc(routeData.getField(GTFSObjectRoute.FIELD_ROUTE_DESC), curRoute);

            //route_type
            switch(routeData.routeType) {
                case tramStreetcarLightrail:
                    curRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_LIGHT_RAIL); //NOTE: GTFS conflates tram and light_rail types
                    break;
                case subwayMetro:
                    curRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_SUBWAY);
                case rail:
                    curRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_TRAIN);
                    break;
                case bus:
                    curRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_BUS);
                    break;
                case ferry:
                    curRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_FERRY);
                    break;
                case cablecar:
                    curRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_TRAM);
                    break;
                case gondola:
                    logEvent(LogLevel.info, "OSM routes don’t typically cover Gondola routes.  Maybe use aerialway=gondola way instead?", lineNumber+1);
                    curRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_AERIALWAY);
                    break;
                case funicular:
                    curRoute.setTag(OSMEntity.KEY_ROUTE, OSMEntity.TAG_TRAIN);
                    break;
            }
            routes.add(curRoute);

            //route_url
            String routeUrl = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_URL);
            if(routeUrl != null && !routeUrl.isEmpty()) {
                curRoute.setTag(OSMEntity.KEY_WEBSITE, routeUrl);
            }

            //route_color
            String routeColor = routeData.getField(GTFSObjectRoute.FIELD_ROUTE_COLOR);
            if(routeColor != null && !routeColor.isEmpty()) {
                curRoute.setTag(OSMEntity.KEY_COLOUR, routeColor);
            }

            //route_text_color
            //(unused)
        }
        in.close();
        fStream.close();
    }
    private void processRouteDesc(String value, OSMRouteMaster route) {
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

    public ArrayList<OSMRouteMaster> getRoutes() {
        return routes;
    }

}
