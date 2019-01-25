package team;

public class TO_BE_DELETED_moving extends DynamicShipClass {


    public TO_BE_DELETED_moving(int mmsi) {
        this.mmsi = mmsi;
    }


    public int getmmsi() {
        return mmsi;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TO_BE_DELETED_moving) {
            TO_BE_DELETED_moving other = (TO_BE_DELETED_moving) obj;
            return mmsi == other.mmsi;
        } else {
            return false;
        }
    }


    @Override
    public String toString() {
        return "speed: { mmsi : " + getmmsi() + " }";
    }

}

