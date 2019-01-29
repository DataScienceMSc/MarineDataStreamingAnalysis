package team.ComplexEvents;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import team.SimpleEvents.GapEvent;
import team.SimpleEvents.SimpleEvent;
import team.SimpleEvents.StoppedEvent;


import java.util.List;
import java.util.Map;


public class SuspiciousStopsCE implements Runnable{


        DataStream<SimpleEvent> parsedStream;
        String outputFile;
        StreamExecutionEnvironment env;

        public SuspiciousStopsCE(DataStream<SimpleEvent> parsedStream, String outputFile,StreamExecutionEnvironment env) {
            this.parsedStream = parsedStream;
            this.outputFile = outputFile;
            this.env=env;

        }

    public void run(){
        try {
            Pattern<SimpleEvent, ?> complex = Pattern.<SimpleEvent>begin("start")
                    .subtype(StoppedEvent.class)
                    .where(new SimpleCondition<StoppedEvent>() {

                        @Override
                        public boolean filter(StoppedEvent value) throws Exception {
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

            CEP.pattern(this.parsedStream, complex).flatSelect(new PatternFlatSelectFunction<SimpleEvent, String>() {

                public void flatSelect(Map<String, List<SimpleEvent>> map, Collector<String> collector) throws Exception {
                    StringBuilder str = new StringBuilder();
                    for (Map.Entry<String, List<SimpleEvent>> entry : map.entrySet()) {
                        System.out.println(map.entrySet());
                        for (SimpleEvent t : entry.getValue()) {
                            //Do we really need this for loop here?
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
            }).writeAsText(this.outputFile, FileSystem.WriteMode.OVERWRITE);
            env.execute();
        }catch (Exception e){
            System.out.println(e.getCause());
        }
    }
}