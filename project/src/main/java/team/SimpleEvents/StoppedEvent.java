package team.SimpleEvents;

import java.util.Objects;

public class StoppedEvent extends SimpleEvent {

    private double speed;

    public StoppedEvent(int mmsi, long TsStart,long TsEnd, int gridId, double speed, double lat, double lon) {
        super(mmsi, TsStart, TsEnd, gridId, lat, lon);
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoppedEvent)) return false;
        if (!super.equals(o)) return false;
        StoppedEvent that = (StoppedEvent) o;
        return getSpeed() == that.getSpeed();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getSpeed());
    }
}
