package GTFSConverter.OSM;

/**
 * Created by nick on 10/29/15.
 */
public class Point {
    public final static double DEGREE_DISTANCE_AT_EQUATOR = 111319.490779206;
    private final static double RAD_FACTOR = Math.PI / 360.0;
    public double latitude, longitude;

    public Point(final double lat, final double lon) {
        latitude = lat;
        longitude = lon;
    }
    public Point(final String lat, final String lon) {
        latitude = Double.parseDouble(lat);
        longitude = Double.parseDouble(lon);
    }
    public Point(final Point point) {
        latitude = point.latitude;
        longitude = point.longitude;
    }
    public static double distance(final Point point1, final Point point2) {
        final double latitudeFactor = Math.cos(RAD_FACTOR * (point1.latitude + point2.latitude));
        return Math.sqrt((point2.longitude - point1.longitude) * (point2.longitude - point1.longitude) * latitudeFactor * latitudeFactor + (point2.latitude - point1.latitude) * (point2.latitude - point1.latitude)) * DEGREE_DISTANCE_AT_EQUATOR;
    }
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        final double latitudeFactor = Math.cos(RAD_FACTOR * (lat1 + lat2));
        return Math.sqrt((lon2 - lon1) * (lon2 - lon1) * latitudeFactor * latitudeFactor + (lat2 - lat1) * (lat2 - lat1)) * DEGREE_DISTANCE_AT_EQUATOR;
    }
    public static double distance(double deltaLatitude, double deltaLongitude) {
        return Math.sqrt(deltaLatitude * deltaLatitude + deltaLongitude * deltaLongitude) * DEGREE_DISTANCE_AT_EQUATOR;
    }
    @Override
    public String toString() {
        return "Point[" + latitude + "," + longitude + "]";
    }
}