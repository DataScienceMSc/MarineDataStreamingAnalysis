package team.SimpleEvents;

public abstract class SimpleEvent {

    private int mmsi;
    private long tsStart;
    private long tsEnd;
    private int gridId;


    public SimpleEvent(int mmsi, long tsStart, long tsEnd, int gridId) {
        this.mmsi = mmsi;
        this.tsStart = tsStart;
        this.tsEnd = tsEnd;
        this.gridId = gridId;
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

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}