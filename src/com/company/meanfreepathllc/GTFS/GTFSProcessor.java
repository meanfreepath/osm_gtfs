package com.company.meanfreepathllc.GTFS;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by nick on 10/15/15.
 */
public abstract class GTFSProcessor {
    protected static String basePath;
    protected File fp;

    protected HashMap<String, Runnable> gtfsFieldHandlers;
    protected HashMap<Short, String> gtfsColumnIndices;

    public static String getBasePath() {
        return basePath;
    }
    public static void setBasePath(String path) {
        basePath = path;
    }

    public List<LogEvent> eventLog = new ArrayList<LogEvent>(1024);

    protected enum LogLevel {
        info, warn, error;
    }

    private class LogEvent {
        LogLevel eventLevel;
        String message;
        public LogEvent(LogLevel level, String msg) {
            eventLevel = level;
            message = msg;
        }
    }

    protected void logEvent(LogLevel level, String message) {
        LogEvent event = new LogEvent(level, message);
        eventLog.add(event);
    }

    protected abstract void initGTFSColumnHandler();

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
