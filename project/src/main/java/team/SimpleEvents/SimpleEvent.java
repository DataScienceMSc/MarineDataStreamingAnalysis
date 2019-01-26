package team.SimpleEvents;

import java.util.Objects;

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

    public void setMmsi(int mmsi) {
        this.mmsi = mmsi;
    }

    public void setTsStart(long tsStart) {
        this.tsStart = tsStart;
    }

    public void setTsEnd(long tsEnd) {
        this.tsEnd = tsEnd;
    }

    public void setGridId(int gridId) {
        this.gridId = gridId;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}