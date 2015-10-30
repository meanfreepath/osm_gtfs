package com.company.meanfreepathllc.OSM;

/**
 * Created by nick on 10/27/15.
 */
public class OSMRouteMaster extends OSMRelation {

    public static OSMRouteMaster create() {
        return new OSMRouteMaster(acquire_new_id());
    }
    public OSMRouteMaster(long id) {
        super(id);
        setTag(KEY_TYPE, TAG_ROUTE_MASTER);
        setTag(KEY_PUBLIC_TRANSPORT_VERSION, "2");
    }

    public void addRoute(OSMRoute route) {
        addMember(route, "");
    }
}
