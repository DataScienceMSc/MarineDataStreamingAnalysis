package team;

import team.SimpleEvents.SimpleEvent;

import java.util.Objects;

public class NaturaEvent extends SimpleEvent {

    private double lat;
    private double lon;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }



    public NaturaEvent(int mmsi, long TsStart, long TsEnd, int gridId, double lat, double lon) {
        super(mmsi, TsStart,TsEnd, gridId);
        this.lat = lat;
        this.lon = lon;
    }

    public NaturaEvent() {
        super(0, 0, 0,0);
        this.lat = 0;
        this.lon = 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NaturaEvent)) return false;
        if (!super.equals(o)) return false;
        NaturaEvent that = (NaturaEvent) o;
        return getLat() == that.getLat() && getLon() == that.getLon();

    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getLat(), getLon());
    }
}
