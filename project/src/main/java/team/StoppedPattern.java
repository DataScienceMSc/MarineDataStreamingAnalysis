package team;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;

import java.util.List;
import java.util.Map;


public class StoppedPattern {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "/home/valia/MarineDataStreamingAnalysis/project/folder/ais_data_small.csv";
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
                    System.out.println("Match Found!");
                    for (Map.Entry<String, List<DynamicShipClass>> entry: pattern.entrySet()) {
                        startTime= entry.getValue().get(0).getTs();
                        endTime= entry.getValue().get(entry.getValue().size()-1).getTs();
                    }
                    DynamicShipClass temp=pattern.get("start").get(0);
                    return new StoppedEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(),temp.getSpeed());
                });

        Pattern<DynamicShipClass, DynamicShipClass> increasingSpeed = Pattern.<DynamicShipClass>begin("start")
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

        DataStream<SimpleEvent> turn =  CEP.pattern(parsedStream, increasingSpeed)
                .select((Map<String, List<DynamicShipClass>> pattern) -> {
                    long startTime=0;
                    long endTime= 0;
                    int degrees = 0;
                    System.out.println("Match Found!");
                    for (Map.Entry<String, List<DynamicShipClass>> entry: pattern.entrySet()) {
                        startTime= entry.getValue().get(0).getTs();
                        endTime= entry.getValue().get(entry.getValue().size()-1).getTs();
                        degrees = Math.abs(entry.getValue().get(0).getHeading() - entry.getValue().get(entry.getValue().size()-1).getHeading()) ;
                    }
                    DynamicShipClass temp=pattern.get("start").get(0);
                    return new InstantaneousTurnEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(), degrees);
                });

       // turn.print();
        ConnectedStreams<SimpleEvent, SimpleEvent> connectedStreams = stopped.connect(turn);
        //System.out.println(connectedStreams.toString());
       // DataStream<SimpleEvent> Final = stopped.connect(turn);
        //Final.connect(turn);
        //Final.print();
        env.execute();
    }


}