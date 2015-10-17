package com.company.meanfreepathllc.OSM;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nick on 10/15/15.
 */
public class OSMRelation extends OSMEntity {
    protected ArrayList<OSMRelationMember> members = new ArrayList<>();

    public class OSMRelationMember {
        public OSMEntity member;
        public String role;

        public OSMRelationMember(OSMEntity memberEntity, @NotNull String memberRole) {
            role = memberRole;
            member = memberEntity;
        }
    }

    static {
        type = OSMType.relation;
    }
    public static OSMRelation create() {
        return new OSMRelation(acquire_new_id());
    }

    public OSMRelation(long id) {
        osm_id = id;
    }
    public String toString() {
        StringBuilder xml = new StringBuilder(tags.size() * 32 +  members.size() * 64);
        xml.append("<relation id=\"" + osm_id + "\">\n");
        for(HashMap.Entry<String, String> entry : tags.entrySet()) {
            xml.append("<tag k=\"" + escapeForXML(entry.getKey()) + "\" v=\"" + escapeForXML(entry.getValue()) + "\"/>\n");
        }
        for(OSMRelationMember member: members) {
            xml.append("<member type=\"" +  member.member.getType() + "\" ref=\"" + member.member.osm_id + "\" role=\"" + escapeForXML(member.role) + "\"/>\n");
        }
        xml.append("</relation>\n");
        return xml.toString();
    }
    public void addMember(OSMEntity member, String role) {
        members.add(new OSMRelationMember(member, role));
    }
}
