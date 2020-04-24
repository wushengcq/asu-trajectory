package edu.asu.cici.trajectory.etl;

import edu.asu.cici.trajectory.Trajectory;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GeoLifeData {

    public static void main(String[] args) throws IOException, ParseException {
        GeoLifeData geoLifeData = new GeoLifeData();
        geoLifeData.extract();
    }

    public void extract() throws IOException, ParseException {
        String root = "/home/ws/Downloads/geolife_trajectories_1.3/Data/";
        File[] userFolders = (new File(root)).listFiles();

        List<File> sortedFolders = Arrays.asList(userFolders);
        sortedFolders.sort(new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.getName().compareTo(t1.getName());
            }
        });

        Writer writer = new FileWriter("/home/ws/trajectory.txt");

        for (int i=0; i<sortedFolders.size(); i++) {
            File userFolder = sortedFolders.get(i);
            if (!userFolder.isDirectory()) {
                continue;
            }

            int peopleId = Integer.valueOf(userFolder.getName());
            System.out.println(userFolder.getName());
            if (peopleId > 200) {
                continue;
            }

            this.extractPltFiles(userFolder, new TrajectoryHandler() {
                @Override
                public void handle(Trajectory trajectory) {
                    try {
                        //writer.write(trajectory.toSQL());
                        writer.write(trajectory.toCVS());
                        writer.write("\n");
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });
        }

        writer.flush();
        writer.close();
    }

    public  void extractPltFiles(File dir, TrajectoryHandler handler) throws IOException, ParseException {
        int peopleId = Integer.valueOf(dir.getName());
        File labelFile = new File(dir.getAbsolutePath() + File.separator + "labels.txt");
        TransportLabelMatcher transportLabelMatcher = new TransportLabelMatcher(labelFile);

        File[] trajectoryFiles = (new File(dir.getAbsolutePath() + File.separator + "Trajectory/")).listFiles();
        for(File trajectoryFile : trajectoryFiles) {
            this.parsePtlFile(trajectoryFile, peopleId, transportLabelMatcher, handler);
        }
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void parsePtlFile(
            File pltFile,
            int peopleId,
            TransportLabelMatcher transportLabelMatcher,
            TrajectoryHandler handler) throws IOException, ParseException {
        FileReader fileReader = new FileReader(pltFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        int count = 0;
        //System.out.println(pltFile);
        while((line = bufferedReader.readLine()) != null) {
            if (count++ < 6) {
                continue;
            }

            String[] cols = line.split(",");
            Trajectory trajectory = new Trajectory();
            trajectory.setPeopleId(peopleId);
            trajectory.setLatitude(Double.parseDouble(cols[0]));
            trajectory.setLongitude(Double.parseDouble(cols[1]));
            trajectory.setAltitude(Double.parseDouble(cols[3]));
            trajectory.setDate(simpleDateFormat.parse(cols[5] + " " +  cols[6]));
            trajectory.setTransportType(transportLabelMatcher.match(trajectory.getDate()));

            handler.handle(trajectory);
        }
    }
}
