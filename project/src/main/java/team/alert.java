package team;


public class alert extends DynamicShipClass {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public alert(int mmsi, double speed) {
        super();
        this.mmsi = mmsi;
        this.speed = speed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mmsi).append(",");
        sb.append(speed).append(",");

        return sb.toString();
    }



    /*@Override
    public boolean equals(Object other) {
        return other instanceof DynamicShipClass &&
                this.mmsi == ((DynamicShipClass) other).mmsi;
    }

    @Override
    public int hashCode() {
        return ((int)this.mmsi);
    }
*/


}
