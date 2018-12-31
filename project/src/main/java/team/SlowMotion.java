package team;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlowMotion {

    public static void main(String[] args) throws Exception {

        GeoUtils geo = new GeoUtils();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "/home/valia/Desktop/FarFromPorts.csv";
        TextInputFormat format = new TextInputFormat(
                new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line))
                .keyBy(element -> element.getmmsi());


        Pattern<DynamicShipClass, DynamicShipClass> increasingSpeed = Pattern.<DynamicShipClass>begin("startSlowMotion", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        //System.out.println("first event");
                         if (value.getSpeed()>=0.5 && value.getSpeed()<= 1.0)
                        {
                             System.out.println("first match");
                             return true;
                         }
                         else
                             return false;
                    }
                })
               .next("end").where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        if (value.getSpeed()>=0.5 && value.getSpeed()<= 1.0)
                        {
                            System.out.println("second match");
                            return true;
                        }
                        else
                            return false;
                    }
                }).timesOrMore(9).greedy().consecutive();


        CEP.pattern(parsedStream, increasingSpeed).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    System.out.println("Match");
                    for (DynamicShipClass t: entry.getValue()) {
                        str.append(t.getmmsi());
                        str.append(",   ");
                        str.append(t.getSpeed());
                        str.append(",   ");
                        str.append(t.getEventTime());
                        str.append(",   ");
                        str.append(t.getLat());
                        str.append(",   ");
                        str.append(t.getLon());
                        str.append("\n");
                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText("/home/valia/Desktop/slowMotion.txt", FileSystem.WriteMode.OVERWRITE);

        env.execute();


    }


}