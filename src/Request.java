import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;

public class Request implements Serializable {

    private static final long serialVersionUID = -2723363051271966964L;
    private double xmin, ymim, xmax, ymax;
    private GregorianCalendar startDate, endDate;

    public Request(double xmin, double ymim, double xmax, double ymax, GregorianCalendar startDate, GregorianCalendar endDate) {
        this.xmin = xmin;
        this.ymim = ymim;
        this.xmax = xmax;
        this.ymax = ymax;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public double getXmin() {
        return xmin;
    }

    public double getYmim() {
        return ymim;
    }

    public double getXmax() {
        return xmax;
    }

    public double getYmax() {
        return ymax;
    }

    public GregorianCalendar getStartDate() {
        return startDate;
    }

    public GregorianCalendar getEndDate() {
        return endDate;
    }

    public void setXmin(double xmin) {
        this.xmin = xmin;
    }

    public void setYmim(double ymim) {
        this.ymim = ymim;
    }

    public void setXmax(double xmax) {
        this.xmax = xmax;
    }

    public void setYmax(double ymax) {
        this.ymax = ymax;
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
                "xmin=" + xmin +
                ", ymim=" + ymim +
                ", xmax=" + xmax +
                ", ymax=" + ymax +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
