package team;


public class Velocity {

	public Velocity(DynamicShipClass shipState, DynamicShipClass previousState) {

		this.velValue = calculateVelocity(shipState, previousState);
		this.velHeading = headingDiff(shipState, previousState);
	}

	public double velValue;
	public int velHeading;


	public static double calculateVelocity(DynamicShipClass shipState, DynamicShipClass previousState) {

		double velocityValue =  getDistance(shipState.lon, shipState.lat, previousState.lon, previousState.lat)/ Math.abs((shipState.ts - previousState.ts));
		return velocityValue;
	}
	public static int headingDiff(DynamicShipClass shipState, DynamicShipClass previousState)
	{
		int heading = shipState.heading - previousState.heading;
		return heading;
	}

	public static double earthRadius = 6371000; //meters

	public static float getDistance(double lat1, double lng1, double lat2, double lng2) {
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
						Math.sin(dLng/2) * Math.sin(dLng/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		float dist = (float) (earthRadius * c);

		return dist;
	}
}