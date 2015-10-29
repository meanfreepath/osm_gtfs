package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/28/15.
 */
public class GTFSProcessorShapes extends GTFSProcessor {
    public static GTFSProcessorShapes instance;

    static {
        FILE_NAME = "shapes.txt";
    }

    public static GTFSProcessorShapes initInstance() throws IOException {
        instance = new GTFSProcessorShapes();
        return instance;
    }
    public GTFSProcessorShapes() throws IOException {
        gtfsColumnIndices = new HashMap<>();

        fp = new File(basePath + FILE_NAME);
        if(!fp.exists()) {
            throw new FileNotFoundException("No shapes.txt file found in directory!");
        }

        GTFSObjectShape tripData;
        List<String> explodedLine;
        int lineNumber = 0;

        FileInputStream fStream = new FileInputStream(fp.getAbsoluteFile());
        BufferedReader in = new BufferedReader(new InputStreamReader(fStream));
        while (in.ready()) {
            explodedLine = parseLine(in);
            if(explodedLine == null) { //i.e. blank line
                continue;
            }

            tripData = new GTFSObjectShape();
            if(lineNumber++ == 0) { //header line
                processFileHeader(explodedLine, tripData);
                continue;
            }

            //run the handler for each column
            try {
                processLine(explodedLine, tripData);
            } catch (InvalidArgumentException e) {
                logEvent(LogLevel.error, e.getLocalizedMessage(), lineNumber + 1);
                continue;
            }
        }
        in.close();
        fStream.close();
    }
}
