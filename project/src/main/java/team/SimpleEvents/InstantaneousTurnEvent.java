package team.SimpleEvents;

import java.util.Objects;

public class InstantaneousTurnEvent extends SimpleEvent {
    private int degrees;

    public InstantaneousTurnEvent(int mmsi, long tsStart, long tsEnd, int gridId, int degrees, double lat, double lon) {
        super(mmsi, tsStart, tsEnd, gridId, lat, lon);
        this.degrees = degrees;
    }

    public int getDegrees() {
        return this.degrees;
    }

    public void setDegrees(int degrees) {
        this.degrees = degrees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InstantaneousTurnEvent)) return false;
        if (!super.equals(o)) return false;
        InstantaneousTurnEvent that = (InstantaneousTurnEvent) o;
        return getDegrees() == that.getDegrees();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDegrees());
    }
}
