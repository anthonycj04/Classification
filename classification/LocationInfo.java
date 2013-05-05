package classification;

import java.util.TreeMap;

public class LocationInfo {
	private int numOfVisits;
	private TreeMap<String, Integer> numOfVisitsPerMonth;
	private double xCoordinate;
	private double yCoordinate;

	public LocationInfo(){
		setNumOfVisits(0);
		setXCoordinate(setYCoordinate(0));
		numOfVisitsPerMonth = new TreeMap<String, Integer>();
	}

	public LocationInfo(double xCoordinate, double yCoordinate){
		setNumOfVisits(0);
		setXCoordinate(xCoordinate);
		setYCoordinate(yCoordinate);
		numOfVisitsPerMonth = new TreeMap<String, Integer>();
	}

	public void incNumOfVisits(){
		numOfVisits++;
	}

	public void incNumOfVisitsPerMonth(String date){
		if (!numOfVisitsPerMonth.containsKey(date))
			numOfVisitsPerMonth.put(date, 0);
		else
			numOfVisitsPerMonth.put(date, numOfVisitsPerMonth.get(date) + 1);
	}

	public int getNumOfVisits() {
		return numOfVisits;
	}

	public void setNumOfVisits(int numOfVisits) {
		this.numOfVisits = numOfVisits;
	}

	public double getXCoordinate() {
		return xCoordinate;
	}

	public void setXCoordinate(double xCoordinate) {
		this.xCoordinate = xCoordinate;
	}

	public double getYCoordinate() {
		return yCoordinate;
	}

	public double setYCoordinate(double yCoordinate) {
		this.yCoordinate = yCoordinate;
		return yCoordinate;
	}

	public TreeMap<String, Integer> getNumOfVisitsPerMonth() {
		return numOfVisitsPerMonth;
	}
}
