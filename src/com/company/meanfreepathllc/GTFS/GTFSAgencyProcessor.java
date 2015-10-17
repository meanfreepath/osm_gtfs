package com.company.meanfreepathllc.GTFS;

import com.company.meanfreepathllc.OSM.OSMBusPlatform;
import com.company.meanfreepathllc.OSM.OSMNode;
import com.oracle.tools.packager.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/15/15.
 */
public class GTFSAgencyProcessor extends GTFSProcessor {
    private final static int INITIAL_CAPACITY = 32;
    private final static String FILE_NAME = "agency.txt";
    private final static String[] GTFS_COLUMNS = {"agency_id", "agency_name", "agency_url", "agency_timezone", "agency_lang", "agency_phone", "agency_fare_url"};
    private final static String[] GTFS_COLUMNS_REQUIRED = {"agency_name", "agency_url", "agency_timezone"};
    public static GTFSAgencyProcessor instance;

    public ArrayList<GTFSAgency> agencies;
    public HashMap<String, GTFSAgency> agencyLookup;
    public AgencyIdStatus agencyIdStatus = AgencyIdStatus.Unknown;

    private GTFSAgency curAgency;
    private String curGTFSColumnValue;

    public enum AgencyIdStatus {
        Unknown, Present, NotPresent
    }
    /**
     * Container class for Agency data
     */
    public class GTFSAgency implements GTFSDataObject {
        public String id;
        public String name, timeZone, url, urlFare, lang, phone;

        @Override
        public void checkRequiredFields() {
            if(name == null || url == null || timeZone == null) {
                //TODO: log an error if missing
            }
        }
    }
    public static GTFSAgencyProcessor initInstance() throws IOException {
        instance = new GTFSAgencyProcessor();
        return instance;
    }
    public GTFSAgencyProcessor() throws IOException {
        gtfsColumnIndices = new HashMap<>(GTFS_COLUMNS.length);
        initGTFSColumnHandler();

        fp = new File(basePath + FILE_NAME);
        if(!fp.exists()) {
            throw new FileNotFoundException("No agency.txt file found in directory!");
        }

        agencies = new ArrayList<>(INITIAL_CAPACITY);
        agencyLookup = new HashMap<>(INITIAL_CAPACITY);
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
            curAgency = new GTFSAgency();

            //run the handler for each column
            colIdx = 0;
            for (String colVal : explodedLine) {
                curGTFSColumnValue = colVal;
                gtfsFieldHandlers.get(gtfsColumnIndices.get(colIdx++)).run();
            }
            agencies.add(curAgency);

            //flag whether the agency ID is present
            if(agencyIdStatus == AgencyIdStatus.Unknown) {
                agencyIdStatus = curAgency.id != null ? AgencyIdStatus.Present : AgencyIdStatus.NotPresent;
            }

            //only add to the lookup if IDs are present
            if(agencyIdStatus == AgencyIdStatus.Present) {
                agencyLookup.put(curAgency.id, curAgency);
            }
        }
        in.close();
        fStream.close();
    }
    protected void initGTFSColumnHandler() {
        gtfsFieldHandlers = new HashMap<String,Runnable>(GTFS_COLUMNS.length);
        gtfsFieldHandlers.put("agency_id", new Runnable() {
            @Override
            public void run() {
                processAgencyId(curGTFSColumnValue, curAgency);
            }
        });
        gtfsFieldHandlers.put("agency_name", new Runnable() {
            @Override
            public void run() {
                processAgencyName(curGTFSColumnValue, curAgency);
            }
        });
        gtfsFieldHandlers.put("agency_url", new Runnable() {
            @Override
            public void run() {
                processAgencyUrl(curGTFSColumnValue, curAgency);
            }
        });
        gtfsFieldHandlers.put("agency_timezone", new Runnable() {
            @Override
            public void run() {
                processAgencyTimeZone(curGTFSColumnValue, curAgency);
            }
        });
        gtfsFieldHandlers.put("agency_lang", new Runnable() {
            @Override
            public void run() {
                processAgencyLang(curGTFSColumnValue, curAgency);
            }
        });
        gtfsFieldHandlers.put("agency_phone", new Runnable() {
            @Override
            public void run() {
                processAgencyPhone(curGTFSColumnValue, curAgency);
            }
        });
        gtfsFieldHandlers.put("agency_fare_url", new Runnable() {
            @Override
            public void run() {
                processAgencyFareUrl(curGTFSColumnValue, curAgency);
            }
        });
    }
    private void processAgencyId(String value, GTFSAgency agency) {
        agency.id = value;
    }
    private void processAgencyName(String value, GTFSAgency agency) {
        agency.name = value;
    }
    private void processAgencyUrl(String value, GTFSAgency agency) {
        agency.url = value;
    }
    private void processAgencyTimeZone(String value, GTFSAgency agency) {
        agency.timeZone = value;
    }
    private void processAgencyLang(String value, GTFSAgency agency) {
        agency.lang = value;
    }
    private void processAgencyPhone(String value, GTFSAgency agency) {
        agency.phone = value;
    }
    private void processAgencyFareUrl(String value, GTFSAgency agency) {
        agency.urlFare = value;
    }
}
