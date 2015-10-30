package com.company.meanfreepathllc.OSM;

/**
 * Created by nick on 10/15/15.
 */
public class OSMBusPlatform extends OSMNode {
    public final static String KEY_HIGHWAY = "highway", KEY_PUBLIC_TRANSPORT = "public_transport";
    public final static String DEF_HIGHWAY = "bus_stop", DEF_PUBLIC_TRANSPORT = "platform";

    public OSMBusPlatform(long id) {
        super(id);

        setTag(KEY_HIGHWAY, DEF_HIGHWAY);
        setTag(KEY_PUBLIC_TRANSPORT, DEF_PUBLIC_TRANSPORT);
    }

    public static OSMBusPlatform create() {
        return new OSMBusPlatform(acquire_new_id());
    }
}
