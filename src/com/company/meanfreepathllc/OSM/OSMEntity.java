package com.company.meanfreepathllc.OSM;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nick on 10/15/15.
 */
public abstract class OSMEntity {
    public final static String KEY_LATITUDE = "lat", KEY_LONGITUDE = "lon", KEY_OSMID = "osm_id", KEY_FROM = "from", KEY_VIA = "via", KEY_TO = "to", KEY_OPERATOR = "operator", KEY_ROUTE = "route", KEY_NAME = "name", KEY_REF = "ref", KEY_LOCAL_REF = "local_ref", KEY_DESCRIPTION = "description", KEY_WEBSITE = "website", KEY_TYPE = "type", KEY_PUBLIC_TRANSPORT_VERSION = "public_transport:version", KEY_COLOUR = "colour";
    public final static String TAG_ROUTE = "route", TAG_BUS = "bus", TAG_LIGHT_RAIL = "light_rail", TAG_TRAM = "tram", TAG_SUBWAY = "subway", TAG_TRAIN = "train", TAG_FERRY = "ferry", TAG_AERIALWAY = "aerialway";
    protected static long new_id_sequence = 0;
    protected static OSMType type;
    public long osm_id;
    protected HashMap<String,String> tags;

    protected static long acquire_new_id() {
        return --new_id_sequence;
    }
    public OSMType getType() {
        return type;
    }
    public void addTag(String name, String value) throws InvalidArgumentException {
        if(tags == null) {
            tags = new HashMap<>();
        }

        if(tags.containsKey(name)) {
            String[] msg = {"Tag \"" + name + "\" already set!"};
            throw new InvalidArgumentException(msg);
        }
        tags.put(name, value.trim());
    }
    public void setTag(String name, String value) {
        if(tags == null) {
            tags = new HashMap<>();
        }
        tags.put(name, value.trim());
    }
    public String getTag(String name) {
        if(tags == null) {
            return null;
        }
        return tags.get(name);
    }
    protected static String escapeForXML(String str) {
        return str.replaceAll("\\&", "&amp;");
    }
    public static String outputXml(ArrayList<OSMNode> entities) {
        StringBuilder outputXml = new StringBuilder();
        outputXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        outputXml.append("<osm version=\"0.6\" generator=\"KCMetroImporter\">\n");
        for(OSMEntity entity:entities) {
            outputXml.append(entity.toString());
        }

        outputXml.append("</osm>\n");
        return outputXml.toString();
    }
}
