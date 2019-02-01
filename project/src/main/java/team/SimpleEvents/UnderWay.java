package team.SimpleEvents;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.util.Collector;
import team.General.DynamicShipClass;

import java.util.List;
import java.util.Map;

public class UnderWay implements Runnable {


    static DataStream<String> inputStream;
    static String outputFile;
    static StreamExecutionEnvironment env;

    public UnderWay(DataStream<String> stream, String outputFile, StreamExecutionEnvironment env) {
        this.inputStream = stream;
        this.outputFile = outputFile;
        this.env=env;

    }


    public void run(){
        try{
            DataStream<DynamicShipClass> parsedStream = inputStream
                    .map(line -> DynamicShipClass.fromString(line))
                    .keyBy(DynamicShipClass::getmmsi);

        Pattern<DynamicShipClass, DynamicShipClass> movingShip = Pattern.<DynamicShipClass>begin("WayStart", AfterMatchSkipStrategy.skipPastLastEvent())

                .where(new SimpleCondition<DynamicShipClass>() {
                    //private static final long serialVersionUID = 314415972814127035L;

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed()>=2.7 && value.getSpeed()<50.;
                    }
                })
                .next("middle")
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed()>=2.7 && value.getSpeed()<50.;
                    }
                })
                .oneOrMore().greedy().consecutive()
                .next("lowEnd")
                .where(new IterativeCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
                        return value.getSpeed()<2.7 || value.getSpeed()>50.;
                    }
                });



        CEP.pattern(parsedStream, movingShip).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                //System.out.println("here");
                System.out.println("Under Way");
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    for (DynamicShipClass t: entry.getValue()) {
                        str.append("Under way,");
                        str.append(t.getmmsi());
                        str.append(",");
                        str.append(t.getSpeed());
                        str.append(",");
                        str.append(t.getLat());
                        str.append(",");
                        str.append(t.getLon());
                        str.append(",");
                        str.append(t.getTs());

                        str.append("\n");
                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText(outputFile, FileSystem.WriteMode.OVERWRITE);
            env.execute();

        }catch(Exception e){
            System.out.println(e.getCause());
        }
    }


}