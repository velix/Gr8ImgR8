package Workers;

import java.util.List;

public class POI_record {

	private String POI;
	private String latitude;
	private String longitude;
	private List<String> photos;
	private int countOfChecIns;
	
	
	
	public POI_record(String POI, String latitude, String longitude, List<String> photos,
			int countOfChecIns) {
		this.POI = POI;
		this.latitude = latitude;
		this.longitude = longitude;
		this.photos = photos;
		this.countOfChecIns = countOfChecIns;
	}
	
	public String getPOI() {
		return POI;
	}

	public void setPOI(String pOI) {
		POI = pOI;
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
	public List<String> getPhotos() {
		return photos;
	}
	public void setPhotos(List<String> photos) {
		this.photos = photos;
	}
	public int getCountOfChecIns() {
		return countOfChecIns;
	}
	public void setCountOfChecIns(int countOfChecIns) {
		this.countOfChecIns = countOfChecIns;
	}
	
	public String toString()
	{
		return "RECORD\n" + "\tlat: " + this.getLatitude() + " lon: " + this.getLongitude() + "\tCheckIns: " + this.getCountOfChecIns() + "\n" +
							"\tPHOTOS\n\t" + this.getPhotos().toString();
	}
	
	
}
