package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by nick on 10/15/15.
 */
public abstract class GTFSProcessor {
    protected static String FILE_NAME;
    protected static String basePath;
    protected File fp;
    public List<LogEvent> eventLog = new ArrayList<LogEvent>(1024);

    protected HashMap<Short, String> gtfsColumnIndices;
    public static String getBasePath() {
        return basePath;
    }
    public static void setBasePath(String path) {
        basePath = path;
    }

    protected enum LogLevel {
        info, warn, error;
    }

    protected String getFileName() {
        return FILE_NAME;
    }

    private class LogEvent {
        final LogLevel eventLevel;
        final String message, fileName;
        final int lineNumber;
        public LogEvent(LogLevel level, String msg, String file, int line) {
            eventLevel = level;
            message = msg;
            fileName = file;
            lineNumber = line;
            System.out.println(fileName + (lineNumber > 0 ? "(line " + lineNumber + "): " : ": ") +  eventLevel + ": " + message);
        }
    }

    protected void logEvent(LogLevel level, String message, int line) {
        LogEvent event = new LogEvent(level, message, getFileName(), line);
        eventLog.add(event);
    }

    public GTFSProcessor() {

    }

    protected void processFileHeader(List<String> headerVals, GTFSObject objectTemplate) {
        short colIdx = 0;
        String[] definedFields = objectTemplate.getDefinedFields();

        boolean fieldOk;
        for (final String colVal: headerVals) {
            fieldOk = false;
            for(int i=0;i<definedFields.length;i++) {
                if(definedFields[i].equals(colVal)) {
                    fieldOk = true;
                    break;
                }
            }

            if(fieldOk) {
                gtfsColumnIndices.put(colIdx++, colVal);
            } else {
                logEvent(LogLevel.warn, "Ignoring values in undefined column “" + colVal + "”", 1);
            }
        }
    }
    protected void processLine(List<String> lineVals, GTFSObject dataObject) throws InvalidArgumentException {
        short colIdx = 0;
        String fieldName;
        for (final String colVal : lineVals) {
            fieldName = gtfsColumnIndices.get(colIdx++);
            if(fieldName != null) { //i.e. the field is defined in the spec
                dataObject.setField(fieldName, colVal);
            }
        }

        dataObject.postProcess();
    }

    /**
     * Copied from https://agiletribe.wordpress.com/2012/11/23/the-only-class-you-need-for-csv-files/
     * @param r
     * @return
     * @throws IOException
     */
    public static List<String> parseLine(Reader r) throws IOException {
        int ch = r.read();
        while (ch == '\r') {
            //ignore linefeed chars wherever, particularly just before end of file
            ch = r.read();
        }
        if (ch<0) {
            return null;
        }
        Vector<String> store = new Vector<String>();
        StringBuffer curVal = new StringBuffer();
        boolean inquotes = false;
        boolean started = false;
        while (ch>=0) {
            if (inquotes) {
                started=true;
                if (ch == '\"') {
                    inquotes = false;
                }
                else {
                    curVal.append((char)ch);
                }
            }
            else {
                if (ch == '\"') {
                    inquotes = true;
                    if (started) {
                        // if this is the second quote in a value, add a quote
                        // this is for the double quote in the middle of a value
                        curVal.append('\"');
                    }
                }
                else if (ch == ',') {
                    store.add(curVal.toString());
                    curVal = new StringBuffer();
                    started = false;
                }
                else if (ch == '\r') {
                    //ignore LF characters
                }
                else if (ch == '\n') {
                    //end of a line, break out
                    break;
                }
                else {
                    curVal.append((char)ch);
                }
            }
            ch = r.read();
        }
        store.add(curVal.toString());
        return store;
    }
}
