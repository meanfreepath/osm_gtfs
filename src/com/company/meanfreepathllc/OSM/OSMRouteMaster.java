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
    }

    public void addRoute(OSMRoute route) {
        addMember(route, "");
    }
}
