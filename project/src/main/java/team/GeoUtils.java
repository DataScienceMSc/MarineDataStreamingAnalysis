package team;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * GeoUtils provides utility methods to deal with locations.
 */
public class GeoUtils implements Serializable {


	// geo boundaries of the area of Brest
	public static double MAX_LON = -0.01573666;
	public static double MIN_LON = -9.713331;
	public static double MIN_LAT = 45.001045;
	public static double MAX_LAT = 50.887634;

	// delta step to create artificial grid overlay of Brest
	public static double DELTA_LON = 0.0054;
	public static double DELTA_LAT = 0.00525;

	public static int NUMBER_OF_GRID_X = (int) (Math.floor(Math.abs(MIN_LON) - Math.abs(MAX_LON)) / DELTA_LON);
	public static int NUMBER_OF_GRID_Y = (int) (Math.floor(Math.abs(MAX_LAT) - Math.abs(MIN_LAT)) / DELTA_LAT);

	public static double earthRadius = 6371000; //meters


	/**
	 * Maps a location specified by latitude and longitude values to a cell of a
	 * grid covering the area of Brest
	 * The grid cells are roughly 300 x 300 m and sequentially number from north-west
	 * to south-east starting by zero.
	 *
	 * @param lon longitude of the location to map
	 * @param lat latitude of the location to map
	 * @return id of mapped grid cell.
	 */
	public static int mapToGridCell(double lon, double lat) {
		int xIndex = (int) Math.floor((Math.abs(MIN_LON) - Math.abs(lon)) / DELTA_LON);
		int yIndex = (int) Math.floor((MAX_LAT - lat) / DELTA_LAT);

		return xIndex + (yIndex * NUMBER_OF_GRID_X);
	}

	/**
	 * Returns the distance between two locations specified as lon/lat pairs.
	 *
	 * @param lat1 latitude of the first ship
	 * @param lng1 longitude of the first ship
	 * @param lat2 latitude of the second ship
	 * @param lng2 longitude of the second ship
	 * @return distance of the ships
	 */
	public static float getDistance(double lat1, double lng1, double lat2, double lng2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
						Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		float dist = (float) (earthRadius * c);

		return dist;
	}

	public ArrayList<Integer> latlonToGrid(String filePath) throws IOException {
		ArrayList<Integer> result = new ArrayList<>();
		int grid;
		Scanner scan = new Scanner(new File(filePath));

		while (scan.hasNextLine()) {
			String numLine = scan.nextLine();
			String[] parts = numLine.split(",");
			double[] ints = new double[parts.length];
			//for (int i = 0; i < parts.length; i++) {
				ints[0] = Double.parseDouble(parts[0]);
				ints[1] = Double.parseDouble(parts[1]);
				grid = mapToGridCell(ints[0], ints[1]);
				result.add(grid);
                System.out.println(" grid: "+ grid +" lat: " + ints[1] + " lon: "+ ints[0] + "\n");


		}
		return result;
	}
}