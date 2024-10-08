package GTFSConverter.GTFS;

import GTFSConverter.OSM.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 10/28/15.
 */
public class GTFSObjectShape extends GTFSObject {
    public final static int INITIAL_CAPACITY = 4096, INITIAL_CAPACITY_POINTS = 1024;

    public final static String
            FIELD_SHAPE_ID = "shape_id",
            FIELD_SHAPE_PT_LAT = "shape_pt_lat",
            FIELD_SHAPE_PT_LON = "shape_pt_lon",
            FIELD_SHAPE_PT_SEQUENCE = "shape_pt_sequence",
            FIELD_SHAPE_DIST_TRAVELED = "shape_dist_traveled";

    public final static String[] definedFields = {FIELD_SHAPE_ID, FIELD_SHAPE_PT_LAT, FIELD_SHAPE_PT_LON, FIELD_SHAPE_PT_SEQUENCE, FIELD_SHAPE_DIST_TRAVELED};
    public final static String[] requiredFields = {FIELD_SHAPE_ID, FIELD_SHAPE_PT_LAT, FIELD_SHAPE_PT_LON, FIELD_SHAPE_PT_SEQUENCE};

    public static class ShapePoint extends Point {
        public double distanceTraveled;
        public int sequence;

        ShapePoint(String lat, String lon, String seq, double dist) {
            super(lat, lon);
            sequence = Integer.parseInt(seq);
            distanceTraveled = dist;
        }
    }

    public List<ShapePoint> points;
    public double totalDistanceTraveled = -1.0;

    public GTFSObjectShape() {
        fields = new HashMap<>(getDefinedFields().length);
    }
    @Override
    public void postProcess(GTFSDataset dataset) throws IllegalArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            throw new IllegalArgumentException(String.format("Missing the following fields: %s", String.join(", ", missingFields)));
        }

        //first check if there's an existing shape with this id.
        GTFSObjectShape exShape = dataset.shapeLookup.get(getField(FIELD_SHAPE_ID));
        if(exShape != null) { //if so, add this shape's point data to it and bail (this object to be discarded)
            exShape.addPointFromShape(this);
        } else { //if not, this shape can be added to the main list
            points = new ArrayList<>(INITIAL_CAPACITY_POINTS);

            //add the point data to the shape
            addPointFromShape(this);

            //and add to the main shape list
            dataset.allShapes.add(this);
            dataset.shapeLookup.put(getField(FIELD_SHAPE_ID), this);
        }
    }
    public void addPointFromShape(GTFSObjectShape shape) {
        final String dt = shape.getField(FIELD_SHAPE_DIST_TRAVELED);
        final double distanceTraveled;
        if(dt != null && !dt.isEmpty()) {
            distanceTraveled = Double.parseDouble(dt);
            totalDistanceTraveled += distanceTraveled;
        } else {
            distanceTraveled = -1.0;
        }
        points.add(new ShapePoint(shape.getField(FIELD_SHAPE_PT_LAT), shape.getField(FIELD_SHAPE_PT_LON), shape.getField(FIELD_SHAPE_PT_SEQUENCE), distanceTraveled));
    }

    @Override
    public String getFileName() {
        return "shapes.txt";
    }
    @Override
    public String[] getDefinedFields() {
        return definedFields;
    }
    @Override
    public String[] getRequiredFields() {
        return requiredFields;
    }
}
