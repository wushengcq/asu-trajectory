package edu.asu.cici.trajectory.etl;

import edu.asu.cici.trajectory.TransportType;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransportLabelMatcher {

    private class TimeRangeMatcher {
        protected Date start;
        protected Date end;
        protected TransportType transportType;

        public boolean inRange(Date date) {
            return date.after(start) && date.before(end);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(simpleDateFormat.format(start)).append(", ");
            sb.append(simpleDateFormat.format(end)).append(", ");
            sb.append(transportType.toString());
            return sb.toString();
        }
    }

    private List<TimeRangeMatcher> timeRangeMatchers = new ArrayList<>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public TransportLabelMatcher(File labelFile) throws IOException, ParseException {
        this.init(labelFile);
    }

    private void init(File labelFile) throws IOException, ParseException {
        if (labelFile == null || !labelFile.exists()) {
            return;
        }

        FileReader fileReader = new FileReader(labelFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = bufferedReader.readLine();
        while((line = bufferedReader.readLine()) != null) {
            line = line.trim().replaceAll("\t", " ");
            if (line.length() < 15) {
                continue;
            }

            String[] vals = line.split(" ");
            Date start = simpleDateFormat.parse(vals[0] + " " + vals[1]);
            Date end   = simpleDateFormat.parse(vals[2] + " " + vals[3]);
            TransportType transportType = TransportType.valueOf(vals[4].toUpperCase());

            TimeRangeMatcher timeRangeMatcher = new TimeRangeMatcher();
            timeRangeMatcher.start = start;
            timeRangeMatcher.end = end;
            timeRangeMatcher.transportType = transportType;
            this.timeRangeMatchers.add(timeRangeMatcher);
        }
    }

    public TransportType match(Date date) {
        for(TimeRangeMatcher timeRangeMatcher : this.timeRangeMatchers) {
            if (timeRangeMatcher.inRange(date)) {
                return timeRangeMatcher.transportType;
            }
        }
        return TransportType.UNKNOWN;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for(TimeRangeMatcher matcher : this.timeRangeMatchers) {
            sb.append(matcher.toString()).append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException, ParseException {
        String file = "/home/ws/Downloads/geolife_trajectories_1.3/Data/067/labels.txt";
        TransportLabelMatcher matcher = new TransportLabelMatcher(new File(file));
    }

}


