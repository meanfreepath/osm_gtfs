package com.company.meanfreepathllc.GTFS;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by nick on 10/15/15.
 */
public class GTFSProcessor {
    protected static String basePath;
    public final static List<LogEvent> eventLog = new ArrayList<>(1024);

    public static String getBasePath() {
        return basePath;
    }
    public static void setBasePath(String path) {
        basePath = path;
    }

    private static int curLineNumber;
    private static String curFileName;

    public enum LogLevel {
        info, warn, error
    }

    private static class LogEvent {
        final LogLevel eventLevel;
        final String message, fileName;
        final int lineNumber;
        public LogEvent(LogLevel level, String msg, String file, int line) {
            eventLevel = level;
            message = msg;
            fileName = file;
            lineNumber = line + 1;
        }
    }

    public static void logEvent(LogLevel level, String message) {
        LogEvent event = new LogEvent(level, message, curFileName, curLineNumber);
        eventLog.add(event);
    }
    public static void outputEventLogs() {
        for(LogEvent event: eventLog) {
            System.out.println(event.fileName + "(line " + event.lineNumber + "): " +  event.eventLevel.toString() + ": " + event.message);
        }
    }
    public static void processData(Class<? extends GTFSObject> objectClass) throws IOException, IllegalAccessException, InstantiationException {
        GTFSObject dataObject;
        List<String> explodedLine;
        short colIdx;

        curLineNumber = 0;
        dataObject = objectClass.newInstance();
        curFileName = dataObject.getFileName();
        String[] definedFields = dataObject.getDefinedFields();

        final HashMap<Short, String> gtfsColumnIndices = new HashMap<>(definedFields.length);
        final HashMap<Short, String> gtfsColumnIndicesNonStandard = new HashMap<>();

        File fp = new File(basePath + curFileName);
        if(!fp.exists()) {
            throw new FileNotFoundException("No " + curFileName + " file found in directory!");
        }
        FileInputStream fStream = new FileInputStream(fp.getAbsoluteFile());
        BufferedReader in = new BufferedReader(new InputStreamReader(fStream));
        int lineIndex = 1;
        while (in.ready()) {
            explodedLine = parseLine(in);
            lineIndex++;
            if(explodedLine == null || explodedLine.isEmpty() || explodedLine.get(0).isEmpty()) { //i.e. blank line
                continue;
            }

            try {
                colIdx = 0;
                dataObject = objectClass.newInstance();
                if(curLineNumber++ == 0) { //header line
                    boolean fieldOk;
                    for (final String colVal: explodedLine) {
                        fieldOk = false;
                        for (final String definedField : definedFields) {
                            if (definedField.equals(colVal)) {
                                fieldOk = true;
                                break;
                            }
                        }

                        if(fieldOk) {
                            gtfsColumnIndices.put(colIdx++, colVal);
                        } else {
                            gtfsColumnIndices.put(colIdx, null);
                            gtfsColumnIndicesNonStandard.put(colIdx, colVal);
                            colIdx++;
                            logEvent(LogLevel.info, "Nonstandard GTFS column “" + colVal + "” found: adding to nonstandard fields.");
                        }
                    }
                    continue;
                }

                //run the handler for each column
                String fieldName;
                for (final String colVal : explodedLine) {
                    fieldName = gtfsColumnIndices.get(colIdx);
                    if(fieldName != null) { //i.e. the field is defined in the spec
                        dataObject.setField(fieldName, colVal);
                    } else {
                        fieldName = gtfsColumnIndicesNonStandard.get(colIdx);
                        dataObject.setNonStandardField(fieldName, colVal);
                    }
                    colIdx++;
                }
                dataObject.postProcess();
            } catch (InstantiationException | IllegalAccessException | InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
        in.close();
        fStream.close();
    }
    /**
     * Copied from https://agiletribe.wordpress.com/2012/11/23/the-only-class-you-need-for-csv-files/
     * @param r
     * @return
     * @throws IOException
     */
    public static List<String> parseLine(final Reader r) throws IOException {
        int ch = r.read();
        while (ch == '\r') {
            //ignore linefeed chars wherever, particularly just before end of file
            ch = r.read();
        }
        if (ch<0) {
            return null;
        }
        Vector<String> store = new Vector<>();
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
                else //noinspection StatementWithEmptyBody
                    if (ch == '\r') {
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
