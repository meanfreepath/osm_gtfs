package com.company.meanfreepathllc.GTFS;

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
public class GTFSProcessorCalendar extends GTFSProcessor {
    public static GTFSProcessorCalendar instance;

    static {
        FILE_NAME = "calendar.txt";
    }
    public static GTFSProcessorCalendar initInstance() throws IOException {
        instance = new GTFSProcessorCalendar();
        return instance;
    }
    public GTFSProcessorCalendar() throws IOException {
        gtfsColumnIndices = new HashMap<>();

        fp = new File(basePath + FILE_NAME);
        if(!fp.exists()) {
            throw new FileNotFoundException("No trips.txt file found in directory!");
        }

        GTFSObjectCalendar calendarData;
        List<String> explodedLine;
        int lineNumber = 0;

        FileInputStream fStream = new FileInputStream(fp.getAbsoluteFile());
        BufferedReader in = new BufferedReader(new InputStreamReader(fStream));
        while (in.ready()) {
            explodedLine = parseLine(in);
            if(explodedLine == null) { //i.e. blank line
                continue;
            }

            calendarData = new GTFSObjectCalendar();
            if(lineNumber++ == 0) { //header line
                processFileHeader(explodedLine, calendarData);
                continue;
            }

            //run the handler for each column
            try {
                processLine(explodedLine, calendarData);
            } catch (InvalidArgumentException e) {
                logEvent(LogLevel.error, e.getLocalizedMessage(), lineNumber+1);
                continue;
            }



        }
        in.close();
        fStream.close();
    }
}
