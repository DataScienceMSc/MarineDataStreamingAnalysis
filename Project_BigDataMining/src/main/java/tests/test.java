


package team;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;

import java.awt.*;

public class test {

    public static void main(String[] args) throws Exception {

        // set up the execution environment
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSet<Event> events =
                env.readCsvFile("/home/eleni/BigDataMining/ais_data_small.csv").pojoType(
                        Event.class,
                        "mmsi",
                        "status",
                        "turn",
                        "speed",
                        "course",
                        "heading",
                        "lon",
                        "lat",
                        "ts"
                );

        System.out.println("----> " + events.count());
    }
}
