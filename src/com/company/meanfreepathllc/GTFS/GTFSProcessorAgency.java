package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/15/15.
 */
public class GTFSProcessorAgency extends GTFSProcessor {
    public enum AgencyIdStatus {
        Unknown, Present, NotPresent
    }
    private final static int INITIAL_CAPACITY = 32;
    private final static String FILE_NAME = "agency.txt";
    public static GTFSProcessorAgency instance;

    public ArrayList<GTFSObjectAgency> agencies;
    private HashMap<String, GTFSObjectAgency> agencyLookup;
    public AgencyIdStatus agencyIdStatus = AgencyIdStatus.Unknown;

    public static GTFSProcessorAgency initInstance() throws IOException {
        instance = new GTFSProcessorAgency();
        return instance;
    }
    public GTFSProcessorAgency() throws IOException {
        gtfsColumnIndices = new HashMap<>();

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
                processFileHeader(explodedLine);
                continue;
            }

            //run the handler for each column
            GTFSObjectAgency curAgency= new GTFSObjectAgency();
            try {
                processLine(explodedLine, curAgency);
            } catch (InvalidArgumentException e) {
                logEvent(LogLevel.error, e.getLocalizedMessage());
                continue;
            }
            agencies.add(curAgency);

            //flag whether the agency ID is present
            String agencyId = curAgency.getField(GTFSObjectAgency.FIELD_AGENCY_ID);
            if(agencyIdStatus == AgencyIdStatus.Unknown) {
                agencyIdStatus = agencyId != null ? AgencyIdStatus.Present : AgencyIdStatus.NotPresent;
            }

            //only add to the lookup if IDs are present
            if(agencyIdStatus == AgencyIdStatus.Present) {
                agencyLookup.put(agencyId, curAgency);
            }
        }
        in.close();
        fStream.close();
    }
    public GTFSObjectAgency lookupAgencyById(String id) {
        switch (agencyIdStatus) {
            case Unknown:
                return null;
            case Present:
                return agencyLookup.get(id);
            case NotPresent:
                if(agencies.size() > 0) {
                    return agencies.get(0);
                } else {
                    return null;
                }
        }
        return null;
    }
}
