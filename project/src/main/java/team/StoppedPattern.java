package team;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;

import java.util.List;
import java.util.Map;


public class StoppedPattern {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "folder/";
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


        DataStream<SimpleEvent> warnings = CEP.pattern(parsedStream, stoppedShip).
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

        env.execute();
    }


}