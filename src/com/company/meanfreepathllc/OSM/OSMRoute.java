package com.company.meanfreepathllc.OSM;

/**
 * Created by nick on 10/16/15.
 */
public class OSMRoute extends OSMRelation {
    public final static String[] DEFINED_ROUTE_TYPES = {"bus"};

    public static OSMRoute create() {
        return new OSMRoute(acquire_new_id());
    }
    public OSMRoute(long id) {
        super(id);
        setTag(KEY_TYPE, TAG_ROUTE);
        setTag(KEY_PUBLIC_TRANSPORT_VERSION, "2");
    }
}
