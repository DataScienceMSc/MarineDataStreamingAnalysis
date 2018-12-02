package team;

public class moving {
    private int mmsi;

    public moving(int mmsi) {
        this.mmsi = mmsi;
    }


    public double getmmsi() {
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
        return "speed: { AdId : " + getmmsi() + " }";
    }

}

