package team;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;


public class StoppedPattern {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "/Users/thanasiskaridis/Desktop/FarFromPorts.csv";
        TextInputFormat format = new TextInputFormat(
                new org.apache.flink.core.fs.Path(path));

        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);
        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line))
                .keyBy(element -> element.getmmsi());

        Pattern<DynamicShipClass, ?> stoppedShip = Pattern.<DynamicShipClass>begin("start"
                , AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed()<0.5;
                    }
                })
                .oneOrMore().greedy().consecutive()
                .next("end")
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed() > 0.5;
                    }
                });


        DataStream<SimpleEvent> stopped = CEP.pattern(parsedStream, stoppedShip).
                select((Map<String, List<DynamicShipClass>> pattern) -> {
                    long startTime=0;
                    long endTime= 0;
                    System.out.println("Match Found1!");
                    for (Map.Entry<String, List<DynamicShipClass>> entry: pattern.entrySet()) {
                        startTime= entry.getValue().get(0).getTs();
                        endTime= entry.getValue().get(entry.getValue().size()-1).getTs();
                    }
                    DynamicShipClass temp=pattern.get("start").get(0);
                    return new StoppedEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(),temp.getSpeed());
                });






        Pattern<DynamicShipClass, DynamicShipClass> turnPattern = Pattern.<DynamicShipClass>begin("start")
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed()>0.0; //not including noise
                    }
                })
                .next("end").where(new IterativeCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {

                        for (DynamicShipClass event : ctx.getEventsForPattern("start")) {
                            //calculating heading difference
                            return Math.abs(value.getHeading() - event.getHeading())>15;
                        }
                        return false;
                    }
                });

        DataStream<SimpleEvent> turn =  CEP.pattern(parsedStream, turnPattern)
                .select((Map<String, List<DynamicShipClass>> pattern) -> {
                    long startTime=0;
                    long endTime= 0;
                    int degrees = 0;
                    System.out.println("Match Found2!");
                    for (Map.Entry<String, List<DynamicShipClass>> entry: pattern.entrySet()) {
                        startTime= entry.getValue().get(0).getTs();
                        endTime= entry.getValue().get(entry.getValue().size()-1).getTs();
                        degrees = Math.abs(entry.getValue().get(0).getHeading() - entry.getValue().get(entry.getValue().size()-1).getHeading()) ;
                    }
                    DynamicShipClass temp=pattern.get("start").get(0);
                    return new InstantaneousTurnEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(), degrees);
                });

        DataStream<SimpleEvent> connectedStreams = stopped.union(turn)
             .keyBy(element -> element.getMmsi());

        Pattern<SimpleEvent, ?> complex = Pattern.<SimpleEvent>begin("start")
                .subtype(StoppedEvent.class)
                .where(new SimpleCondition<StoppedEvent>() {

                    @Override
                    public boolean filter(StoppedEvent value) throws Exception {
                        System.out.println("Found3");
                        return true;
                    }
                })
                .followedBy("end")
                .subtype(InstantaneousTurnEvent.class)
                .where(new SimpleCondition<InstantaneousTurnEvent>() {

                    @Override
                    public boolean filter(InstantaneousTurnEvent value) throws Exception {
                        System.out.println("Found4");
                        return true;
                    }
                });


        CEP.pattern(connectedStreams, complex).flatSelect(new PatternFlatSelectFunction<SimpleEvent, String>() {

            @Override
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
        }).writeAsText("/Users/thanasiskaridis/Desktop/Stopped.csv", FileSystem.WriteMode.OVERWRITE);

        env.execute();

    }


}