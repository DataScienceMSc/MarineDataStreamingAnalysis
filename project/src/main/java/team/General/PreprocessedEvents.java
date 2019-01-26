package team.General;

//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
//import java.util.Locale;


public class PreprocessedEvents implements Comparable<PreprocessedEvents>, Serializable {

	//private static transient DateTimeFormatter timeFormatter =
	//		DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.US).withZoneUTC(); //na valoume to timestamp se wra
	public PreprocessedEvents() {
		this.ts = new Long(ts);
	}

	public PreprocessedEvents(int mmsi, boolean turn, double speed, long ts, int gridId, boolean gapStart, boolean gapEnd, boolean lowStart, boolean lowEnd, boolean isStopped) {

		this.mmsi = mmsi;
		this.turn = turn;
		this.speed = speed;
		this.ts = ts;
		this.gridId = gridId;
		this.gapStart = gapStart;
		this.gapEnd = gapEnd;
		this.lowStart = lowStart;
		this.lowEnd = lowEnd;
		this.isStopped = isStopped;
	}

	public int mmsi;
	public boolean turn;
	public double speed;
	public long ts;
	public int gridId;
	public boolean gapStart;
	public boolean gapEnd;
	public boolean lowStart;
	public boolean lowEnd;
	public boolean isStopped;


	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mmsi).append(",");
		sb.append(turn).append(",");
		sb.append(speed).append(",");
		sb.append(ts).append(",");
		sb.append(gridId).append(",");
		sb.append(gapStart).append(",");
		sb.append(gapEnd).append(",");
		sb.append(lowStart).append(",");
		sb.append(lowEnd).append(",");
		sb.append(isStopped);

		return sb.toString();
	}

	public static int assignGeo(DynamicShipClass ship, GeoUtils geo) {

		PreprocessedEvents events = new PreprocessedEvents();
		int gridId;
		try {
			gridId = geo.mapToGridCell(ship.lon, ship.lat);
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("Invalid record");
		}

		return gridId;
	}





	public int compareTo(PreprocessedEvents other) {
		return Long.compare(this.mmsi, other.mmsi);
	}


	public long getEventTime() {
		return this.ts;
	}

	public int getmmsi() {
		return mmsi;
	}
	public void setmmsi(int mmsi) {
		this.mmsi = mmsi;
	}

	public int getGridId() {
		return gridId;
	}
	public void setGridId(int gridId) {
		this.gridId = gridId;
	}

	public boolean getTurn() {
		return turn;
	}
	public void setTurn(boolean turn) {
		this.turn = turn;
	}

	public void setIsStopped(boolean isStopped) {
		this.isStopped = isStopped;
	}

	public void setgapStart(boolean gapStart) {
		this.gapStart = gapStart;
	}

	public void setgapEnd(boolean gapEnd) {
		this.gapEnd = gapEnd;
	}

	public void setLowStart(boolean lowStart) {
		this.lowStart = lowStart;
	}

	public void setLowEnd(boolean lowEnd) {
		this.lowEnd = lowEnd;
	}

	public boolean getlowStart() {
		return lowStart;
	}

	public boolean getlowEnd() {
		return lowEnd;
	}

	public boolean getgapStart() {
		return gapStart;
	}

	public boolean getgapEnd() {
		return gapEnd;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void setTs(long ts){
		this.ts = ts;
	}
	public long getTs() {
		return ts;
	}
}