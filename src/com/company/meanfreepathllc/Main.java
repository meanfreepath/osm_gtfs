package com.company.meanfreepathllc;

import com.company.meanfreepathllc.GTFS.GTFSAgencyProcessor;
import com.company.meanfreepathllc.GTFS.GTFSProcessor;
import com.company.meanfreepathllc.GTFS.GTFSRouteProcessor;
import com.company.meanfreepathllc.GTFS.GTFSStopsProcessor;
import com.company.meanfreepathllc.OSM.OSMEntity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;

public class Main {
    public static void main(String[] args) {
	// write your code here
        GTFSProcessor.setBasePath("/Users/nick/Downloads/1_gtfs/");

        try {
            System.out.print("Processing agencies...\n");
            GTFSAgencyProcessor agencyProcessor = GTFSAgencyProcessor.initInstance();
            System.out.printf("%d agencies found\n", agencyProcessor.agencies.size());

            System.out.print("Processing routes...\n");
            GTFSRouteProcessor routeProcessor = new GTFSRouteProcessor();
            System.out.printf("%d routes found\n", routeProcessor.getRoutes().size());

            System.out.print("Processing stops...\n");
            GTFSStopsProcessor stopsProcessor = new GTFSStopsProcessor();
            System.out.printf("%d stops found\n", stopsProcessor.getStops().size());

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
