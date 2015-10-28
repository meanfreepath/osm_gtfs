package com.company.meanfreepathllc;

import com.company.meanfreepathllc.GTFS.GTFSProcessorAgency;
import com.company.meanfreepathllc.GTFS.GTFSProcessor;
import com.company.meanfreepathllc.GTFS.GTFSProcessorRoute;
import com.company.meanfreepathllc.GTFS.GTFSProcessorStops;
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
            System.out.printf("%d agencies found\n", agencyProcessor.agencies.size());

            System.out.print("Processing routes...\n");
            GTFSProcessorRoute routeProcessor = new GTFSProcessorRoute();
            System.out.printf("%d routes found\n", routeProcessor.getRoutes().size());

            System.out.print("Processing stops...\n");
            GTFSProcessorStops stopsProcessor = new GTFSProcessorStops();
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
