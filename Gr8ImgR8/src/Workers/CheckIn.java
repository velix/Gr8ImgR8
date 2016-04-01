package Workers;

import java.io.Serializable;

/**
 * Created by Velix on 28/3/2016.
 */
public class CheckIn implements Serializable
{
    private static final long serialVersionUID = -2729973029811966964L;

    private String latitude;
    private String longitude;
    private String POI;
    private String POI_name;
    private String link;



    public CheckIn(String latitude, String longitude, String pOI,
			String pOI_name, String link) 
    {
		this.latitude = latitude;
		this.longitude = longitude;
		POI = pOI;
		POI_name = pOI_name;
		this.link = link;
	}



	public String getLatitude() {
		return latitude;
	}



	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}



	public String getLongitude() {
		return longitude;
	}



	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}



	public String getPOI() {
		return POI;
	}



	public void setPOI(String pOI) {
		POI = pOI;
	}



	public String getPOI_name() {
		return POI_name;
	}



	public void setPOI_name(String pOI_name) {
		POI_name = pOI_name;
	}



	public String getLink() {
		return link;
	}



	public void setLink(String link) {
		this.link = link;
	}





}
