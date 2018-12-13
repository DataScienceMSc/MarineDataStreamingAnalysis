package team;

import java.util.Objects;

public class GapEvent extends SimpleEvent {
    private enum typeOfGap {gapStar,gapEnd};

    public GapEvent(int mmsi, long tsStart, long tsEnd, int gridId, typeOfGap gapType) {
        super(mmsi, tsStart, tsEnd, gridId);
        this.gapType = gapType;
    }

    typeOfGap gapType;



    public typeOfGap getGapType() {
        return gapType;
    }

    public void setGapType(typeOfGap gapType) {
        this.gapType = gapType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GapEvent)) return false;
        if (!super.equals(o)) return false;
        GapEvent gapEvent = (GapEvent) o;
        return getGapType() == gapEvent.getGapType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getGapType());
    }
}
