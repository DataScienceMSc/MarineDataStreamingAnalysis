package team.SimpleEvents;

import java.util.Objects;

public class LowSpeedEvent extends SimpleEvent {
    private double speed;

    public LowSpeedEvent(int mmsi, long tsStart, long tsEnd, int gridId, double speed) {
        super(mmsi, tsStart, tsEnd, gridId);
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LowSpeedEvent)) return false;
        if (!super.equals(o)) return false;
        LowSpeedEvent that = (LowSpeedEvent) o;
        return getSpeed() == that.getSpeed();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getSpeed());
    }
}
