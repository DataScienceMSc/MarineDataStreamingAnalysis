package team;

import team.SimpleEvents.SimpleEvent;

import java.util.Objects;

public class TO_BE_DELETED_SpeedChangeEvent extends SimpleEvent {
    private double change;

    public TO_BE_DELETED_SpeedChangeEvent(int mmsi, long tsStart, long tsEnd, int gridId, int change) {
        super(mmsi, tsStart, tsEnd, gridId);
        this.change = change;
    }

    public TO_BE_DELETED_SpeedChangeEvent(int mmsi, long tsStart, long tsEnd, int gridId, double change) {
        super(mmsi, tsStart, tsEnd, gridId);
        this.change = change;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TO_BE_DELETED_SpeedChangeEvent)) return false;
        if (!super.equals(o)) return false;
        TO_BE_DELETED_SpeedChangeEvent that = (TO_BE_DELETED_SpeedChangeEvent) o;
        return Double.compare(that.getChange(), getChange()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getChange());
    }
}
