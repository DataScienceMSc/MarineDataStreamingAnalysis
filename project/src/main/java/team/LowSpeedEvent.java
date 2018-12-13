package team;

import java.util.Objects;

public class LowSpeedEvent extends SimpleEvent {
    private int speed;

    public LowSpeedEvent(int mmsi, long tsStart, long tsEnd, int gridId, int speed) {
        super(mmsi, tsStart, tsEnd, gridId);
        this.speed = speed;
    }

    public int getSpeed() {
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
