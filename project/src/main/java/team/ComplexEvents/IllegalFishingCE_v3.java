package team.ComplexEvents;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import team.SimpleEvents.*;


import java.util.List;
import java.util.Map;

public class IllegalFishingCE_v3 implements Runnable {


    DataStream<SimpleEvent> parsedStream;
    String outputFile;
    StreamExecutionEnvironment env;

    public IllegalFishingCE_v3(DataStream<SimpleEvent> parsedStream, String outputFile,StreamExecutionEnvironment env) {

        this.parsedStream = parsedStream;
        this.outputFile = outputFile;
        this.env=env;
    }

    public  void run(){
        try {
            Pattern<SimpleEvent, ?> complexGap = Pattern.<SimpleEvent>begin("start")
                    .subtype(NaturaEvent.class)
                    .where(new SimpleCondition<NaturaEvent>() {

                        @Override
                        public boolean filter(NaturaEvent value) throws Exception {
                            return true;
                        }
                    })
                    .followedBy("end")
                    .subtype(GapEvent.class)
                    .where(new SimpleCondition<GapEvent>() {

                        @Override
                        public boolean filter(GapEvent value) throws Exception {
                            return true;
                        }
                    });

            CEP.pattern(parsedStream, complexGap).flatSelect(new PatternFlatSelectFunction<SimpleEvent, String>() {

                @Override
                public void flatSelect(Map<String, List<SimpleEvent>> map, Collector<String> collector) throws Exception {
                    StringBuilder str = new StringBuilder();
                    for (Map.Entry<String, List<SimpleEvent>> entry : map.entrySet()) {
                        for (SimpleEvent t : entry.getValue()) {
                            str.append(t.getMmsi()+",");
                            str.append(t.getLat()+",");
                            str.append(t.getLon()+",");
                            str.append("\n");              }
                    }
                    collector.collect(str.toString());
                }
            }).writeAsText(outputFile, FileSystem.WriteMode.OVERWRITE);
            env.execute();
        }catch(Exception e)
        {
            System.out.println(e.getCause());
        }
    }
}
