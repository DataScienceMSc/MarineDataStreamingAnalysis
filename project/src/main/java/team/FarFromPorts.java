package team;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class FarFromPorts {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "test");

        // create Kafka's consumer
        FlinkKafkaConsumer09<String> myConsumer = new FlinkKafkaConsumer09<>("test", new SimpleStringSchema(), properties);

        // create the data stream
        DataStream<String> inputStream = env.addSource(myConsumer);


        GeoUtils geo = new GeoUtils();
        ArrayList<Integer> portsOfBrittany = geo.latlonToGrid("/Users/thanasiskaridis/Desktop/latlon.csv");

//        String path = "/Users/thanasiskaridis/Desktop/nari_dynamic.csv";
//        TextInputFormat format = new TextInputFormat(
//                new org.apache.flink.core.fs.Path(path));
//        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line,geo))
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<DynamicShipClass>() {

                    @Override
                    public long extractAscendingTimestamp(DynamicShipClass element) {
                        return element.getEventTime();
                    }
                }).keyBy(DynamicShipClass::getmmsi);

        Pattern<DynamicShipClass, DynamicShipClass> increasingSpeed = Pattern.<DynamicShipClass>begin("startSlowMotion", AfterMatchSkipStrategy.skipPastLastEvent())
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
            private static final long serialVersionUID = -8972838879934875538L;

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
        }).writeAsText("/Users/thanasiskaridis/Desktop/FarFromPorts.txt", FileSystem.WriteMode.OVERWRITE);
        env.execute();
    }
}