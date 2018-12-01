package team;

public class Event {

    public int mmsi;
    public int status;
    public int turn;
    public double speed;
    public double course;
    public int heading;
    public double lon;
    public double lat;
    public int ts;

    public Event() { }

    public Event(int mmsi, int status, int turn, double speed, double course, int heading, double lon, double lat, int ts) {

        this.mmsi = mmsi;
        this.status = status;
        this.turn = turn;
        this.speed = speed;
        this.course = course;
        this.heading = heading;
        this.lon = lon;
        this.lat = lat;
        this.ts = ts;
    }
}
