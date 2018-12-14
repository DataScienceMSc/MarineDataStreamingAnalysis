package team;

import java.util.Objects;

public class SpeedChangeEvent extends SimpleEvent {
    private double change;

    public SpeedChangeEvent(int mmsi, long tsStart, long tsEnd, int gridId, int change) {
        super(mmsi, tsStart, tsEnd, gridId);
        this.change = change;
    }

    public SpeedChangeEvent(int mmsi, long tsStart, long tsEnd, int gridId, double change) {
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
        if (!(o instanceof SpeedChangeEvent)) return false;
        if (!super.equals(o)) return false;
        SpeedChangeEvent that = (SpeedChangeEvent) o;
        return Double.compare(that.getChange(), getChange()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getChange());
    }
}
