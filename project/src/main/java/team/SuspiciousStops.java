package team;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//* Skeleton for a Flink Streaming Job.
//* <p>For a tutorial how to write a Flink streaming application, check the
//* tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
//*
//* <p>To package your application into a JAR file for execution, run
//* 'mvn clean package' on the command line.
//*
//* <p>If you change the name of the main class (with the public static void main(String[] args))
//* method, change the respective entry in the POM.xml file (simply search for 'mainClass').


public class SuspiciousStops {

    public static void main(String[] args) throws Exception {

        GeoUtils geo = new GeoUtils();


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);

        String path = "/home/valia/Desktop/FarFromPorts.csv";
        TextInputFormat format = new TextInputFormat(
                new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line))
                .keyBy(element -> element.getGridId());


        Pattern<DynamicShipClass, DynamicShipClass> increasingSpeed = Pattern.<DynamicShipClass>begin("startSuspicious", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        //System.out.println("first event");
                         if (value.getgapStart() == true && value.getSpeed()<0.5)
                        {
                            return true;
                         }
                         else
                             return false;
                    }
                })
               .next("middle").where(new IterativeCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
                        String contex="middle";
                        if(!ctx.getEventsForPattern("middle").iterator().hasNext())
                        {
                            contex="startSuspicious";
                        }
                        for (DynamicShipClass event : ctx.getEventsForPattern(contex)) {
                            if( ((event.getTs() - 600) <= value.getTs() && value.getTs() <= event.getTs()) || (value.getTs() <= event.getTs() + 600  && value.getTs() <=event.getTs()) && event.getSpeed()<0.5 && event.getgapStart() == true)
                                return true;
                        }
                        return false;
                    }
                }).timesOrMore(2).consecutive();//.within(Time.seconds(600));


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
                        str.append(t.getTs());
                        str.append("\n");
                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText("/home/valia/Desktop/suspiciousStops.tx", FileSystem.WriteMode.OVERWRITE);

        env.execute();


    }


}