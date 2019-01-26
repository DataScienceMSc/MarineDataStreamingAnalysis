package team.SimpleEvents;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.util.Collector;
import team.General.DynamicShipClass;
import team.General.GeoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FarFromPorts {

    public FarFromPorts(){}


    public static void outputSimpleEvents(DataStream<DynamicShipClass> parsedStream, String outputFile) throws Exception {

        /*StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "test");

        // create Kafka's consumer
        FlinkKafkaConsumer09<String> myConsumer = new FlinkKafkaConsumer09<>("FinalOpenSeaEntries2", new SimpleStringSchema(), properties);

        // create the data stream
        DataStream<String> inputStream = env.addSource(myConsumer);

        */

        GeoUtils geo = new GeoUtils();
        ArrayList<Integer> portsOfBrittany = geo.latlonToGrid("./inputFiles/latlon.csv");

       /* DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line,geo))
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<DynamicShipClass>() {

                    @Override
                    public long extractAscendingTimestamp(DynamicShipClass element) {
                        return element.getEventTime();
                    }
       */

        Pattern<DynamicShipClass, DynamicShipClass> increasingSpeed = Pattern.<DynamicShipClass>begin("openSea", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        //System.out.println("first event");
                        if (!portsOfBrittany.contains(value.getGridId()))
                        {
                            System.out.println("The ship's location is not a port");
                            return true;
                        }
                        else
                            System.out.println("port");
                            return false;
                    }
                });

        CEP.pattern(parsedStream, increasingSpeed).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    //System.out.println("Match");
                    for (DynamicShipClass t: entry.getValue()) {
                        str.append(t.getmmsi());
                        str.append(",");
                        str.append(t.getStatus());
                        str.append(",");
                        str.append(t.getTurn());
                        str.append(",");
                        str.append(t.getSpeed());
                        str.append(",");
                        str.append(t.getCourse());
                        str.append(",");
                        str.append(t.getHeading());
                        str.append(",");
                        str.append(t.getLon());
                        str.append(",");
                        str.append(t.getLat());
                        str.append(",");
                        str.append(t.getTs());
                        str.append(",");
                        str.append(t.getGridId());
//                        str.append("\n");
                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText(outputFile, FileSystem.WriteMode.OVERWRITE);
    }
}