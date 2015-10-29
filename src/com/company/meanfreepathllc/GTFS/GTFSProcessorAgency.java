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
    private final static String FILE_NAME = "agency.txt";
    public static GTFSProcessorAgency instance;

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

        List<String> explodedLine;
        int lineNumber = 0;

        FileInputStream fStream = new FileInputStream(fp.getAbsoluteFile());
        BufferedReader in = new BufferedReader(new InputStreamReader(fStream));
        while (in.ready()) {
            explodedLine = parseLine(in);
            if(explodedLine == null) { //i.e. blank line
                continue;
            }

            GTFSObjectAgency curAgency = new GTFSObjectAgency();

            if(lineNumber++ == 0) { //header line
                processFileHeader(explodedLine, curAgency);
                continue;
            }

            //run the handler for each column
            try {
                processLine(explodedLine, curAgency);
            } catch (InvalidArgumentException e) {
                logEvent(LogLevel.error, e.getLocalizedMessage(), lineNumber+1);
                continue;
            }
        }
        in.close();
        fStream.close();
    }
}
