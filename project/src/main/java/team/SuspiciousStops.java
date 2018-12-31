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
//        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);

        String path = "/Users/thanasiskaridis/Desktop/FarFromPorts.csv";
        TextInputFormat format = new TextInputFormat(
                new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line))
                .keyBy(element -> element.getGridId());


        Pattern<DynamicShipClass, ?> Stopped = Pattern.<DynamicShipClass>begin("start",
                AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        if (value.getSpeed()>5.0)
                        {
                            return true;
                        }
                        else
                            return false;
                    }
                })
                .oneOrMore().greedy().consecutive()
                .next("end")
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed() < 0.5;
                    }
                });

        DataStream<SimpleEvent> stoppedShips = CEP.pattern(parsedStream, Stopped).
                select((Map<String, List<DynamicShipClass>> pattern) -> {
                    long startTime=0;
                    long endTime= 0;
                    System.out.println("StoppedShip Found!");
                    for (Map.Entry<String, List<DynamicShipClass>> entry: pattern.entrySet()) {
                        startTime= entry.getValue().get(0).getTs();
                        endTime= entry.getValue().get(entry.getValue().size()-1).getTs();
                    }
                    DynamicShipClass temp=pattern.get("start").get(0);
                    return new StoppedEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(),temp.getSpeed());
                });

        Pattern<DynamicShipClass, ?> Gap = Pattern.<DynamicShipClass>begin("startGap")
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return true;
                    }
                })
                .next("end").where(new IterativeCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass previous, Context<DynamicShipClass> ctx) throws Exception {
                        String contex="end";
                        if(!ctx.getEventsForPattern("end").iterator().hasNext())
                        {
                            contex="startGap";
                        }
                        for (DynamicShipClass event : ctx.getEventsForPattern(contex)) {

                            if (previous.ts - event.ts >= 10 * 60 * 2)
                                return true;
                            else
                                return false;
                        }
                        return false;
                    }
                });


        DataStream<SimpleEvent> gapEvent = CEP.pattern(parsedStream, Gap).
                select((Map<String, List<DynamicShipClass>> pattern) -> {
                    System.out.println("Gap Found!");
                    long startTime=pattern.get("startGap").get(0).getTs();
                    long endTime=pattern.get("end").get(0).getTs();
                    DynamicShipClass temp=pattern.get("startGap").get(0);

                    return new GapEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(),(endTime-startTime));
                });


        DataStream<SimpleEvent> connectedStreams = stoppedShips.union(gapEvent)
                .keyBy(element -> element.getMmsi());

        Pattern<SimpleEvent, ?> complex = Pattern.<SimpleEvent>begin("start")
                .subtype(StoppedEvent.class)
                .where(new SimpleCondition<StoppedEvent>() {

                    @Override
                    public boolean filter(StoppedEvent value) throws Exception {
                        System.out.println("FirstStopedEvent");
                        return true;
                    }
                })
                .followedBy("end")
                .subtype(GapEvent.class)
                .where(new SimpleCondition<GapEvent>() {

                    @Override
                    public boolean filter(GapEvent value) throws Exception {
                        System.out.println("SecondGapEvent");
                        return true;
                    }
                });


//                    @Override
//                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
//                        String contex="middle";
//                        if(!ctx.getEventsForPattern("middle").iterator().hasNext())
//                        {
//                            contex="startSuspicious";
//                        }
//                        for (DynamicShipClass event : ctx.getEventsForPattern(contex)) {
//                            if( ((event.getTs() - 600) <= value.getTs() && value.getTs() <= event.getTs()) || (value.getTs() <= event.getTs() + 600  && value.getTs() <=event.getTs()) && event.getSpeed()<0.5 && event.getgapStart() == true)
//                                return true;
//                        }
//                        return false;
//                    }
//                }).timesOrMore(2).consecutive();//.within(Time.seconds(600));


        CEP.pattern(connectedStreams, complex).flatSelect(new PatternFlatSelectFunction<SimpleEvent, String>() {

            public void flatSelect(Map<String, List<SimpleEvent>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, List<SimpleEvent>> entry: map.entrySet()) {
                    for (SimpleEvent t: entry.getValue()) {
                        // StoppedEvent mpiri = (StoppedEvent) t;
                        //  InstantaneousTurn mpiri2 = (InstantaneousTurn) t;
                        System.out.println("Writing");
                        str.append(t.getMmsi());
                        str.append(", " + t.getTsStart());
                        str.append(", " + t.getTsEnd());
                        str.append(", " + t.getGridId());
                        str.append("\n");
                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText("/Users/thanasiskaridis/Desktop/SuspiciousStops.csv", FileSystem.WriteMode.OVERWRITE);

        env.execute();


    }


}