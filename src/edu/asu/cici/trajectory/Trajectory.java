package edu.asu.cici.trajectory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Trajectory {
    private int peopleId;
    private double longitude;
    private double latitude;
    private double altitude;
    private Date date;
    private TransportType transportType;

    public int getPeopleId(){
        return this.peopleId;
    }

    public void setPeopleId(int id) {
        this.peopleId = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DecimalFormat df = new DecimalFormat("000");

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getPeopleId()).append(", ");
        sb.append(this.getLatitude()).append(", ");
        sb.append(this.getLongitude()).append(", ");
        sb.append(this.simpleDateFormat.format(this.getDate())).append(", ");
        sb.append(this.getTransportType() == null ? "" : this.getTransportType());
        return sb.toString();
    }

    public String toSQL() {
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO geolife(\"target\", \"location\", \"date_time\", \"trans_type\") ");
        sb.append("VALUES (");
        sb.append("'").append(df.format(this.getPeopleId())).append("', ");
        sb.append(String.format("ST_GeomFromText('POINT(%f %f)'), ", this.getLongitude(), this.getLatitude()));
        sb.append("'").append(this.simpleDateFormat.format(this.getDate())).append("', ");
        sb.append(this.getTransportType().ordinal());
        sb.append(");");
        return sb.toString();
    }

    public String toCVS() {
        StringBuffer sb = new StringBuffer();
        sb.append(df.format(this.getPeopleId())).append(",");
        //sb.append(String.format("ST_GeomFromText('POINT(%f %f)'),", this.getLongitude(), this.getLatitude()));
        sb.append(this.getTransportType().ordinal()).append(",");
        sb.append(this.simpleDateFormat.format(this.getDate())).append(",");
        sb.append(this.getLongitude()).append(",");
        sb.append(this.getLatitude());
        return sb.toString();
    }

}
