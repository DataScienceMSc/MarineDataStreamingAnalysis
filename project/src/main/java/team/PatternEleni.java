
package team;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.flink.util.Collector;


public class PatternEleni {

    public static void main(String[] args) throws Exception {

        // create execution environment
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

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line))
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<DynamicShipClass>() {

                    @Override
                    public long extractAscendingTimestamp(DynamicShipClass element) {
                        return element.getEventTime();
                    }
                }).keyBy(DynamicShipClass::getmmsi);

        ((KeyedStream) parsedStream).window(SlidingEventTimeWindows.of(Time.milliseconds(20000), Time.milliseconds(2000)));

        inputStream.print();

        Pattern<DynamicShipClass, DynamicShipClass> movingShip = Pattern.<DynamicShipClass>begin("stoppedBefore", AfterMatchSkipStrategy
                .skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {
                    //private static final long serialVersionUID = 314415972814127035L;

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed() == 0.0;
                    }
                })
                .next("middle")
                .where(new IterativeCondition<DynamicShipClass>() {
                    //private static final long serialVersionUID = 6664468385615273240L;

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
                        return value.getSpeed() > 0.0;
                    }
                }).oneOrMore()
                .greedy()
                .consecutive()
                .next("end").where(new IterativeCondition<DynamicShipClass>() {
                    //private static final long serialVersionUID = 6664468385615273240L;

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
                        return value.getSpeed() == 0.0;
                    }
                })
                .within((Time.milliseconds(1000000)));


        CEP.pattern(parsedStream, movingShip).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {
            private static final long serialVersionUID = -8972838879934875538L;

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                System.out.println("here");
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    for (DynamicShipClass t: entry.getValue()) {
                        str.append(t.getmmsi());
                        str.append(",");
                        str.append(t.getSpeed());
                        str.append("\n");
                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText("output.txt", FileSystem.WriteMode.OVERWRITE);

        env.execute();


    }


}