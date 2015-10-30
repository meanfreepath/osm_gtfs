package com.company.meanfreepathllc.GTFS;

import com.company.meanfreepathllc.SpatialTypes.Point;
import com.sun.javaws.exceptions.InvalidArgumentException;

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

    public final static List<GTFSObjectShape> allShapes = new ArrayList<>(INITIAL_CAPACITY);
    public final static HashMap<String, GTFSObjectShape> shapeLookup = new HashMap<>(INITIAL_CAPACITY);

    public class ShapePoint extends Point{
        public double distanceTraveled;
        public int sequence;

        public ShapePoint(String lat, String lon, String seq, String dist) {
            super(lat, lon);
            sequence = Integer.parseInt(seq);
            distanceTraveled = dist != null && !dist.isEmpty() ? Double.parseDouble(dist) : -1.0;
        }
    }

    public List<ShapePoint> points;

    public GTFSObjectShape() {
        fields = new HashMap<>(getDefinedFields().length);
    }
    @Override
    public void postProcess() throws InvalidArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            String[] errMsg = {""};
            errMsg[0] = String.format("Missing the following fields: %s", String.join(", ", missingFields));
            throw new InvalidArgumentException(errMsg);
        }

        //first check if there's an existing shape with this id.
        GTFSObjectShape exShape = shapeLookup.get(getField(FIELD_SHAPE_ID));
        if(exShape != null) { //if so, add this shape's point data to it and bail (this object to be discarded)
            exShape.addPointFromShape(this);
        } else { //if not, this shape can be added to the main list
            points = new ArrayList<>(INITIAL_CAPACITY_POINTS);

            //add the point data to the shape
            addPointFromShape(this);

            //and add to the main shape list
            addToList();
        }
    }
    public void addPointFromShape(GTFSObjectShape shape) {
        points.add(new ShapePoint(shape.getField(FIELD_SHAPE_PT_LAT), shape.getField(FIELD_SHAPE_PT_LON), shape.getField(FIELD_SHAPE_PT_SEQUENCE), shape.getField(FIELD_SHAPE_DIST_TRAVELED)));
    }

    @Override
    protected void addToList() {
        allShapes.add(this);
        shapeLookup.put(getField(FIELD_SHAPE_ID), this);
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
