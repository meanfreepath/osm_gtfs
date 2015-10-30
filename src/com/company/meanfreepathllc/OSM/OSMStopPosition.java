package com.company.meanfreepathllc.OSM;

/**
 * Created by nick on 10/30/15.
 */
public class OSMStopPosition extends OSMPublicTransport {

    public static OSMStopPosition create() {
        return new OSMStopPosition(acquire_new_id());
    }
    public OSMStopPosition(long id) {
        super(id);
        setTag(KEY_PUBLIC_TRANSPORT, TAG_STOP_POSITION);
    }
}
