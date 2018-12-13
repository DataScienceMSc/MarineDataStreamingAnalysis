package team;

import java.util.Objects;

public class GapEvent extends SimpleEvent {
    long duration;

    public GapEvent(int mmsi, long tsStart, long tsEnd, int gridId, long duration) {
        super(mmsi, tsStart, tsEnd, gridId);
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GapEvent)) return false;
        if (!super.equals(o)) return false;
        GapEvent gapEvent = (GapEvent) o;
        return getDuration() == gapEvent.getDuration();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDuration());
    }
}
