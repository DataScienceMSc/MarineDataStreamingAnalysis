package team;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;


public class RandezVousCE1 {

    RandezVousCE1(){};

    static void GenerateComplexEvents(DataStream<SimpleEvent> parsedStream,  String outputFile) throws Exception {

        Pattern<SimpleEvent, ?> complex = Pattern.<SimpleEvent>begin("start")
                .subtype(StoppedEvent.class)
                .where(new SimpleCondition<StoppedEvent>() {

                    @Override
                    public boolean filter(StoppedEvent value) throws Exception {
                        //System.out.println("Found3");         //valia ToDo: add gap event and context for timestamp
                        return true;
                    }
                })
                .followedBy("end")
                .subtype(GapEvent.class)
                .where(new SimpleCondition<GapEvent>() {

                    @Override
                    public boolean filter(GapEvent value) throws Exception {
                        System.out.println("Found4");
                        return true;
                    }
                });


        CEP.pattern(parsedStream, complex).flatSelect(new PatternFlatSelectFunction<SimpleEvent, String>() {

            @Override
            public void flatSelect(Map<String, List<SimpleEvent>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, List<SimpleEvent>> entry : map.entrySet()) {
                    for (SimpleEvent t : entry.getValue()) {
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
        }).writeAsText(outputFile, FileSystem.WriteMode.OVERWRITE);

    }

}
