package team;

//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
//import java.util.Locale;


public class TestClass implements Comparable<TestClass>, Serializable {

	//private static transient DateTimeFormatter timeFormatter =
	//		DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.US).withZoneUTC(); //na valoume to timestamp se wra

	public TestClass() {

		this.mmsi = mmsi;
		this.turn = turn;
		this.speed = speed;
		this.ts = ts;
	}

	public TestClass(int mmsi, int turn, int speed, int ts) {

		this.mmsi = mmsi;
		this.turn = turn;
		this.speed = speed;
		this.ts = ts;
	}

	public int mmsi;
	public int turn;
	public int speed;

	public int getTs() {
		return ts;
	}

	public void setTs(int ts) {
		this.ts = ts;
	}

	public int ts;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mmsi).append(",");
		sb.append(turn).append(",");
		sb.append(speed).append(",");
		sb.append(ts);
		return sb.toString();
	}

	public static TestClass fromString(String line) {

		String[] tokens = line.split(",");
		if (tokens.length != 4) {
			throw new RuntimeException("Invalid record: " + line);
		}

		TestClass test = new TestClass();
		try {
			test.mmsi = tokens[0].length() > 0 ? Integer.parseInt(tokens[0]) : 0;
			test.turn = tokens[1].length() > 0 ? Integer.parseInt(tokens[1]) : 0;
			test.speed = tokens[2].length() > 0 ? Integer.parseInt(tokens[2]) : 0;
			test.ts = tokens[3].length() > 0 ? Integer.parseInt(tokens[3]) : 0;
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("Invalid record: " + line, nfe);
		}

		return test;
	}


	public void setMmsi(int mmsi) {
		this.mmsi = mmsi;
	}

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}


	public int getmmsi() {
		return mmsi;
	}

	public double getSpeed() {
		return speed;
	}

	public int compareTo(TestClass o) {
		return this.getTs() - o.getTs();
	}

}