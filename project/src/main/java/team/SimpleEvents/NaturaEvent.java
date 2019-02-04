package team.SimpleEvents;

import java.util.Objects;

public class NaturaEvent extends SimpleEvent {

    public NaturaEvent(int mmsi, long TsStart, long TsEnd, int gridId, double lat, double lon) {
        super(mmsi, TsStart, TsEnd, gridId, lat, lon);

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
