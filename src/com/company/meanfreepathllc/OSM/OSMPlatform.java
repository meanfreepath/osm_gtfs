package com.company.meanfreepathllc.OSM;

/**
 * Created by nick on 10/15/15.
 */
public class OSMPlatform extends OSMPublicTransport {
    public OSMPlatform(long id) {
        super(id);
        setTag(KEY_PUBLIC_TRANSPORT, TAG_PLATFORM);
    }

    public static OSMPlatform create() {
        return new OSMPlatform(acquire_new_id());
    }
}
