package SharedClasses;

import java.io.Serializable;
import java.util.List;

public class POI_record implements Serializable {

	private static final long serialVersionUID = -272314471127198874L;
	private String POI;
	private String POI_name;
	private String latitude;
	private String longitude;
	private List<String> photos;
	private int countOfChecIns;

	public POI_record(String POI, String POI_name, String latitude, String longitude, List<String> photos, int countOfChecIns) {
		this.POI = POI;
		this.POI_name = POI_name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.photos = photos;
		this.countOfChecIns = countOfChecIns;
	}

	public String getPOI() {
		return POI;
	}

	public String getPOI_name() {
		return POI_name;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public List<String> getPhotos() {
		return photos;
	}

	public int getCountOfChecIns() {
		return countOfChecIns;
	}

	public void setPOI(String POI) {
		this.POI = POI;
	}

	public void setPOI_name(String POI_name) {
		this.POI_name = POI_name;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public void setPhotos(List<String> photos) {
		this.photos = photos;
	}

	public void setCountOfChecIns(int countOfChecIns) {
		this.countOfChecIns = countOfChecIns;
	}

	public String toString()
	{
		return this.getPOI_name() + "\n" + "\tlat: " + this.getLatitude() + " lon: " + this.getLongitude() + "\tCheckIns: " + this.getCountOfChecIns() + "\n" +
							"\tPHOTOS\n\t" + this.getPhotos().toString() + "\n";
	}
}
