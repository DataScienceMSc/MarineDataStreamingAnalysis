package team.SimpleEvents;

public abstract class SimpleEvent {

    private int mmsi;
    private long tsStart;
    private long tsEnd;
    private int gridId;
    private double lat;
    private double lon;


    public SimpleEvent(int mmsi, long tsStart, long tsEnd, int gridId, double lat, double lon ) {
        this.mmsi = mmsi;
        this.tsStart = tsStart;
        this.tsEnd = tsEnd;
        this.gridId = gridId;
        this.lat=lat;
        this.lon=lon;
    }

    public int getMmsi() {
        return mmsi;
    }

    public long getTsStart() {
        return tsStart;
    }

    public long getTsEnd() {
        return tsEnd;
    }

    public int getGridId() {
        return gridId;
    }

    public double getLat(){return lat;}

    public double getLon(){return lon;}

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}