package com.company.meanfreepathllc.GTFS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages all the data for a particular GTFS dataset
 * Created by nick on 4/5/17.
 */
public class GTFSDataset {
    public enum AgencyIdStatus {
        Unknown, Present, NotPresent
    }
    public AgencyIdStatus agencyIdStatus = AgencyIdStatus.Unknown;
    public final String basePath;

    public final List<GTFSObjectAgency> allAgencies = new ArrayList<>(GTFSObjectAgency.INITIAL_CAPACITY);
    public final HashMap<String, GTFSObjectAgency> agencyLookup = new HashMap<>(GTFSObjectAgency.INITIAL_CAPACITY);

    public final List<GTFSObjectCalendar> allCalendars = new ArrayList<>(GTFSObjectCalendar.INITIAL_CAPACITY);
    public final HashMap<String, GTFSObjectCalendar> calendarLookup = new HashMap<>(GTFSObjectCalendar.INITIAL_CAPACITY);

    public final List<GTFSObjectRoute> allRoutes = new ArrayList<>(GTFSObjectRoute.INITIAL_CAPACITY);
    public final HashMap<String, GTFSObjectRoute> routeLookup = new HashMap<>(GTFSObjectRoute.INITIAL_CAPACITY);

    public final List<GTFSObjectTrip> allTrips = new ArrayList<>(GTFSObjectTrip.INITIAL_CAPACITY);
    public final HashMap<String, GTFSObjectTrip> tripLookup = new HashMap<>(GTFSObjectTrip.INITIAL_CAPACITY);

    public final List<GTFSObjectShape> allShapes = new ArrayList<>(GTFSObjectShape.INITIAL_CAPACITY);
    public final HashMap<String, GTFSObjectShape> shapeLookup = new HashMap<>(GTFSObjectShape.INITIAL_CAPACITY);

    public final HashMap<String, GTFSObjectStop> stopLookup = new HashMap<>(GTFSObjectStop.INITIAL_CAPACITY);

    public final List<GTFSObjectStopTime> allStopTimes = new ArrayList<>(GTFSObjectStopTime.INITIAL_CAPACITY);

    public GTFSDataset(String path) {
        basePath = path;
    }

    public GTFSObjectAgency lookupAgencyById(String id) {
        switch (agencyIdStatus) {
            case Unknown:
                return null;
            case Present:
                return agencyLookup.get(id);
            case NotPresent:
                if(allAgencies.size() > 0) {
                    return allAgencies.get(0);
                } else {
                    return null;
                }
        }
        return null;
    }
    void addAgency(GTFSObjectAgency agency) {
        allAgencies.add(agency);

        //flag whether the agency ID is present
        String agencyId = agency.getField(GTFSObjectAgency.FIELD_AGENCY_ID);
        if(agencyIdStatus == AgencyIdStatus.Unknown) {
            agencyIdStatus = agencyId != null ? AgencyIdStatus.Present : AgencyIdStatus.NotPresent;
        }

        //only add to the lookup if IDs are present
        if(agencyIdStatus == AgencyIdStatus.Present) {
            agencyLookup.put(agencyId, agency);
        }
    }
}
