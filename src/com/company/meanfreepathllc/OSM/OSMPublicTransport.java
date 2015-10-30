package com.company.meanfreepathllc.OSM;

/**
 * Created by nick on 10/30/15.
 */
public abstract class OSMPublicTransport extends OSMNode {
    public final static String KEY_HIGHWAY = "highway", KEY_PUBLIC_TRANSPORT = "public_transport", KEY_BUS = "bus", KEY_TRAIN = "train", KEY_FERRY = "ferry", KEY_TRAM = "tram", KEY_AERIALWAY = "aerialway", KEY_FUNICULAR = "funicular";
    public final static String TAG_BUS_STOP = "bus_stop", TAG_PLATFORM = "platform", TAG_STOP_POSITION = "stop_position", TAG_LEGACY_FERRY_TERMINAL = "ferry_terminal";

    public OSMPublicTransport(long id) {
        super(id);
    }
}
