package Request;

import java.io.Serializable;
import java.util.GregorianCalendar;

public class Request implements Serializable {

    private static final long serialVersionUID = -2723363051271966964L;
    private double latitudeMin, longtitudeMin, latitudeMax, longtitudeMax;
    private GregorianCalendar startDate, endDate;
    private int k;

    public Request(double latitudeMin, double longtitudeMin, double latitudeMax, double longtitudeMax, GregorianCalendar startDate, GregorianCalendar endDate, int k) {
        this.latitudeMin = latitudeMin;
        this.longtitudeMin = longtitudeMin;
        this.latitudeMax = latitudeMax;
        this.longtitudeMax = longtitudeMax;
        this.startDate = startDate;
        this.endDate = endDate;
        this.k = k;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public double getLatitudeMin() {
        return latitudeMin;
    }

    public double getLongtitudeMin() {
        return longtitudeMin;
    }

    public double getLatitudeMax() {
        return latitudeMax;
    }

    public double getLongtitudeMax() {
        return longtitudeMax;
    }

    public GregorianCalendar getStartDate() {
        return startDate;
    }

    public GregorianCalendar getEndDate() {
        return endDate;
    }

    public void setLatitudeMin(double latitudeMin) {
        this.latitudeMin = latitudeMin;
    }

    public void setLongtitudeMin(double longtitudeMin) {
        this.longtitudeMin = longtitudeMin;
    }

    public void setLatitudeMax(double latitudeMax) {
        this.latitudeMax = latitudeMax;
    }

    public void setLongtitudeMax(double longtitudeMax) {
        this.longtitudeMax = longtitudeMax;
    }

    public void setStartDate(GregorianCalendar startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(GregorianCalendar endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "Request{" +
                "latitudeMin=" + latitudeMin +
                ", longtitudeMin=" + longtitudeMin +
                ", latitudeMax=" + latitudeMax +
                ", longtitudeMax=" + longtitudeMax +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}