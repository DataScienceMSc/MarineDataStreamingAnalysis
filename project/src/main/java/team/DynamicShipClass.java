package team;

//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
//import java.util.Locale;



public class DynamicShipClass implements Comparable<DynamicShipClass>, Serializable {

	//private static transient DateTimeFormatter timeFormatter =
	//		DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.US).withZoneUTC(); //na valoume to timestamp se wra
	public DynamicShipClass() {
		this.ts = new Long(ts);
	}

	public DynamicShipClass(int mmsi, int status, int turn, double speed, double course, int heading, double lon, double lat, long ts) {

		this.mmsi = mmsi;
		this.status = status;
		this.turn = turn;
		this.speed = speed;
		this.course = course;
		this.heading = heading;
		this.lon = lon;
		this.lat = lat;
		this.ts = ts;
	}

	public int mmsi;
	public int status;
	public int turn;
	public double speed;
	public double course;
	public int heading;
	public double lon;
	public double lat;
	public long ts;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mmsi).append(",");
		sb.append(status).append(",");
		sb.append(turn).append(",");
		sb.append(speed).append(",");
		sb.append(course).append(",");
		sb.append(heading).append(",");
		sb.append(lon).append(",");
		sb.append(lat).append(",");
		sb.append(ts);

		return sb.toString();
	}

	public static DynamicShipClass fromString(String line) {

		String[] tokens = line.split(",");
		if (tokens.length != 9) {
			throw new RuntimeException("Invalid record: " + line);
		}

		DynamicShipClass ship = new DynamicShipClass();

		try {
			ship.mmsi = tokens[0].length() > 0 ? Integer.parseInt(tokens[0]) : 0;
			ship.status = tokens[1].length() > 0 ? Integer.parseInt(tokens[1]) : 0;
			ship.turn = tokens[2].length() > 0 ? Integer.parseInt(tokens[2]) : 0;
			ship.speed = tokens[3].length() > 0 ? Double.parseDouble(tokens[3]) : 0.0f;
			ship.course = tokens[4].length() > 0 ? Double.parseDouble(tokens[4]) : 0.0f;
			ship.heading = tokens[5].length() > 0 ? Integer.parseInt(tokens[5]) : 0;
			ship.lon = tokens[6].length() > 0 ? Double.parseDouble(tokens[6]) : 0.0f;
			ship.lat = tokens[7].length() > 0 ? Double.parseDouble(tokens[7]) : 0.0f;
			ship.ts = tokens[8].length() > 0 ? Long.parseLong(tokens[8]) : 0;

		} catch (NumberFormatException nfe) {
			throw new RuntimeException("Invalid record: " + line, nfe);
		}

		return ship;
	}

	public int compareTo(DynamicShipClass other) {
		return Long.compare(this.mmsi, other.mmsi);
	}


	public long getEventTime() {
		return this.ts*1000;
	}

	public int getmmsi() {
		return mmsi;
	}

	public double getSpeed() {
		return speed;
	}
/*
	public double getEuclideanDistance(double longitude, double latitude) {
		if (this.isStart) {
			return GeoUtils.getEuclideanDistance((float) longitude, (float) latitude, this.startLon, this.startLat);
		} else {
			return GeoUtils.getEuclideanDistance((float) longitude, (float) latitude, this.endLon, this.endLat);
		}
	}
*/
}