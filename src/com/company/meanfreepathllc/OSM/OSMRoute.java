package com.company.meanfreepathllc.OSM;

/**
 * Created by nick on 10/16/15.
 */
public abstract class OSMRoute extends OSMRelation {
    public final static String[] DEFINED_ROUTE_TYPES = {"bus"};

    public OSMRoute(long id) {
        super(id);
        setTag(KEY_TYPE, TAG_ROUTE);
        setTag(KEY_PUBLIC_TRANSPORT_VERSION, "2");
    }
}
