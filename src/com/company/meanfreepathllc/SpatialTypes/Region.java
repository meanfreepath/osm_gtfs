package com.company.meanfreepathllc.SpatialTypes;

/**
 * Created by nick on 10/29/15.
 */
public class Region {
    public Point origin, extent;

    /*public static Region combinedRegionBox(Region region1, Region region2) {
        if(region1 == null) {
            if(region2 == null) {
                return null;
            }
            return region2;
        } else if(region2 == null) {
            if(region1 == null) {
                return null;
            }
            return region1;
        }

        double minLat, minLon, maxLat, maxLon;
        minLat = Math.min(region1.origin.latitude, region2.origin.latitude);
        minLon = Math.min(region1.origin.longitude, region2.origin.longitude);
        maxLat = Math.max(region1.extent.latitude, region2.extent.latitude);
        maxLon = Math.max(region1.extent.longitude, region2.extent.longitude);
        return new Region(new Point(minLat, minLon), new Point(maxLat, maxLon));
    }*/
    public Region(Point origin, Point extent) {
        this.origin = new Point(origin.latitude, origin.longitude);
        this.extent = new Point(extent.latitude, extent.longitude);
    }
    public Region(double latitude, double longitude, double latitudeDelta, double longitudeDelta) {
        origin = new Point(latitude, longitude);
        extent = new Point(latitude + latitudeDelta, longitude + longitudeDelta);
    }
    public void combinedBoxWithRegion(Region otherRegion) {
        if(otherRegion == null) {
            return;
        }
        origin.latitude = Math.min(origin.latitude, otherRegion.origin.latitude);
        origin.longitude = Math.min(origin.longitude, otherRegion.origin.longitude);
        extent.latitude = Math.max(extent.latitude, otherRegion.extent.latitude);
        extent.longitude = Math.max(extent.longitude, otherRegion.extent.longitude);
    }
}
