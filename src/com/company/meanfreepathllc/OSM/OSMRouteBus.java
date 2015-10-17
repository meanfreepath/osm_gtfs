package com.company.meanfreepathllc.OSM;

/**
 * Created by nick on 10/16/15.
 */
public class OSMRouteBus extends OSMRoute {
    public OSMRouteBus(long id) {
        super(id);
        setTag(KEY_ROUTE, TAG_BUS);
    }
    public static OSMRouteBus create() {
        return new OSMRouteBus(acquire_new_id());
    }
}
