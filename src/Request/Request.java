package Request;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;

public class Request implements Serializable {

    private static final long serialVersionUID = -2723363051271966964L;
    private double latitudeMin, latitudeMax, longtitudeMin, longtitudeMax;
    private GregorianCalendar startDate, endDate;

    public Request(double latitudeMin, double latitudeMax, double longtitudeMin, double longtitudeMax, GregorianCalendar startDate, GregorianCalendar endDate) {
        this.latitudeMin = latitudeMin;
        this.latitudeMax = latitudeMax;
        this.longtitudeMin = longtitudeMin;
        this.longtitudeMax = longtitudeMax;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public double getLatitudeMin() {
        return latitudeMin;
    }

    public double getLatitudeMax() {
        return latitudeMax;
    }

    public double getLongtitudeMin() {
        return longtitudeMin;
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

    public void setLatitudeMax(double latitudeMax) {
        this.latitudeMax = latitudeMax;
    }

    public void setLongtitudeMin(double longtitudeMin) {
        this.longtitudeMin = longtitudeMin;
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
                ", latitudeMax=" + latitudeMax +
                ", longtitudeMin=" + longtitudeMin +
                ", longtitudeMax=" + longtitudeMax +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}