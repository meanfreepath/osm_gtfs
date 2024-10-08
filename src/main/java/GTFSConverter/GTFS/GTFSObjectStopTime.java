package GTFSConverter.GTFS;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nick on 10/28/15.
 */
public class GTFSObjectStopTime extends GTFSObject {
    public final static int INITIAL_CAPACITY = 2097152;
//    private final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME.withResolverStyle(ResolverStyle.LENIENT); //lenient to allow times the following day (e.g. 24:09, 25:30, etc)

    public final static String
            FIELD_TRIP_ID = "trip_id",
            FIELD_ARRIVAL_TIME = "arrival_time",
            FIELD_DEPARTURE_TIME = "departure_time",
            FIELD_STOP_ID = "stop_id",
            FIELD_STOP_SEQUENCE = "stop_sequence",
            FIELD_STOP_HEADSIGN = "stop_headsign",
            FIELD_PICKUP_TYPE = "pickup_type",
            FIELD_DROPOFF_TYPE = "drop_off_type",
            FIELD_SHAPE_DIST_TRAVELED = "shape_dist_traveled",
            FIELD_TIMEPOINT = "timepoint";

    public final static String[] definedFields = {FIELD_TRIP_ID, FIELD_ARRIVAL_TIME, FIELD_DEPARTURE_TIME, FIELD_STOP_ID, FIELD_STOP_SEQUENCE, FIELD_STOP_HEADSIGN, FIELD_PICKUP_TYPE, FIELD_DROPOFF_TYPE, FIELD_SHAPE_DIST_TRAVELED, FIELD_TIMEPOINT};
    public final static String[] requiredFields = {FIELD_TRIP_ID, FIELD_ARRIVAL_TIME, FIELD_DEPARTURE_TIME, FIELD_STOP_ID, FIELD_STOP_SEQUENCE};

    public final static List<GTFSObjectStopTime> allStopTimes = new ArrayList<>(INITIAL_CAPACITY);

    public GTFSObjectStop stop;
    //public LocalTime arrival_time, departure_time;

    public GTFSObjectStopTime() {
        fields = new HashMap<>(getDefinedFields().length);
    }
    @Override
    public void postProcess(GTFSDataset dataset) throws IllegalArgumentException {
        List<String> missingFields = checkRequiredFields();
        if(missingFields != null && missingFields.size() > 0) {
            throw new IllegalArgumentException(String.format("Missing the following fields: %s", String.join(", ", missingFields)));
        }

        //assign to the relevant trips and stops
        GTFSObjectTrip parentTrip = dataset.tripLookup.get(getField(FIELD_TRIP_ID));
        stop = dataset.stopLookup.get(getField(FIELD_STOP_ID));
        parentTrip.addStopTime(this, dataset);

        //arrival_time = LocalTime.parse(getField(FIELD_ARRIVAL_TIME), TIME_FORMATTER);
        //departure_time = LocalTime.parse(getField(FIELD_DEPARTURE_TIME), TIME_FORMATTER);

        /*dataset.allStopTimes.add(this);
        dataset.stopTimeLookup.put(getField());*/
    }
    @Override
    public String getFileName() {
        return "stop_times.txt";
    }
    @Override
    public String[] getDefinedFields() {
        return definedFields;
    }
    @Override
    public String[] getRequiredFields() {
        return requiredFields;
    }
    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(fields.size() * 64);
        for(final Map.Entry<String, String> entry : fields.entrySet()) {
            str.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }
        return str.toString();
    }
}
