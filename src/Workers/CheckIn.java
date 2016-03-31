package Workers;

import java.io.Serializable;

/**
 * Created by Velix on 28/3/2016.
 */
public class CheckIn implements Serializable
{
    private static final long serialVersionUID = -2729973029811966964L;

    private String POI;
    private String POI_name;
    private String POI_category;
    private int POI_category_id;
    private String link;

    public CheckIn(String poi, String poi_name, String link)
    {
        this.POI = poi;
        this.POI_name = poi_name;
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
    //latitude,longitude,time,photos


    public String getPOI() {
        return POI;
    }

    public void setPOI(String POI) {
        this.POI = POI;
    }

    public String getPOI_name() {
        return POI_name;
    }

    public void setPOI_name(String POI_name) {
        this.POI_name = POI_name;
    }

    public String getPOI_category() {
        return POI_category;
    }

    public void setPOI_category(String POI_category) {
        this.POI_category = POI_category;
    }

    public int getPOI_category_id() {
        return POI_category_id;
    }

    public void setPOI_category_id(int POI_category_id) {
        this.POI_category_id = POI_category_id;
    }
}
