package edu.asu.cici.trajectory.etl;

import edu.asu.cici.trajectory.TransportType;

import java.text.DecimalFormat;

public class Tester {

    public static void main(String[] args) {
        DecimalFormat df = new DecimalFormat("000");
        System.out.println(df.format(10));

        System.out.println(TransportType.WALK.ordinal());
    }
}
