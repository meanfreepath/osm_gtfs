package com.company.meanfreepathllc.OSM;

import java.util.HashMap;

/**
 * Created by nick on 10/15/15.
 */
public class OSMNode extends OSMEntity {
    public double lat, lon;
    //public $duplicates = [];

    static {
        type = OSMType.node;
    }

    public static OSMNode create() {
        return new OSMNode(acquire_new_id());
    }
    public OSMNode(long id) {
        osm_id = id;
    }
    public String toString() {
        StringBuilder xml = new StringBuilder(tags.size() * 16);
        xml.append("<node id=\"" + osm_id + "\" lat=\"" + lat + "\" lon=\"" + lon + "\" version=\"1\">\n");// user=\"$osmuser\" uid=\"$osmid\" visible=\"true\" version=\"1\">\n"];
        for(HashMap.Entry<String, String> entry : tags.entrySet()) {
            xml.append("<tag k=\"" + escapeForXML(entry.getKey()) + "\" v=\"" + escapeForXML(entry.getValue()) + "\"/>\n");
        }
        xml.append("</node>\n");
        return xml.toString();
    }
    public void setTag(String name, String value) {
        if(name.equals(KEY_LATITUDE)) {
            lat = Double.parseDouble(value);
        } else if(name.equals(KEY_LONGITUDE)) {
            lon = Double.parseDouble(value);
        } else {
            super.setTag(name, value);
        }
    }
}
