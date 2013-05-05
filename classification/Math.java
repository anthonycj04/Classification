package classification;

public class Math {
	public static double calculateDistance(LocationInfo location1, LocationInfo location2){
		double xDistance = location1.getXCoordinate() - location2.getXCoordinate();
		double yDistance = location1.getYCoordinate() - location2.getYCoordinate();
		return xDistance * xDistance + yDistance * yDistance;
	}
}
