package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/28/15.
 */
public class GTFSProcessorStopTimes extends GTFSProcessor {
    public static GTFSProcessorStopTimes instance;

    static {
        FILE_NAME = "stop_times.txt";
    }

    public static GTFSProcessorStopTimes initInstance() throws IOException {
        instance = new GTFSProcessorStopTimes();
        return instance;
    }
    public GTFSProcessorStopTimes() throws IOException {
        gtfsColumnIndices = new HashMap<>();

        fp = new File(basePath + FILE_NAME);
        if(!fp.exists()) {
            throw new FileNotFoundException("No stop_times.txt file found in directory!");
        }

        GTFSObjectStopTime stopTimeData;
        List<String> explodedLine;
        int lineNumber = 0;

        FileInputStream fStream = new FileInputStream(fp.getAbsoluteFile());
        BufferedReader in = new BufferedReader(new InputStreamReader(fStream));
        while (in.ready()) {
            explodedLine = parseLine(in);
            if(explodedLine == null) { //i.e. blank line
                continue;
            }

            stopTimeData = new GTFSObjectStopTime();
            if(lineNumber++ == 0) { //header line
                processFileHeader(explodedLine, stopTimeData);
                continue;
            }

            //run the handler for each column
            try {
                processLine(explodedLine, stopTimeData);
            } catch (InvalidArgumentException e) {
                logEvent(LogLevel.error, e.getLocalizedMessage(), lineNumber + 1);
                continue;
            }
        }
        in.close();
        fStream.close();
    }
}
