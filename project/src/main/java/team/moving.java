package team;

public class moving extends DynamicShipClass {


    public moving(int mmsi) {
        this.mmsi = mmsi;
    }


    public int getmmsi() {
        return mmsi;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof moving) {
            moving other = (moving) obj;
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

