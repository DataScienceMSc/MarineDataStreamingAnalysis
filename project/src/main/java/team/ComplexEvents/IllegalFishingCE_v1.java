package team.ComplexEvents;

import org.apache.flink.streaming.api.datastream.DataStream;
import team.SimpleEvents.SimpleEvent;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.util.Collector;
import team.SimpleEvents.*;

import java.util.List;
import java.util.Map;

public class IllegalFishingCE_v1 {

    public IllegalFishingCE_v1(){};

    public static void GenerateComplexEvents(DataStream<SimpleEvent> parsedStream, String outputFile) throws Exception {
        Pattern<SimpleEvent, ?> complexTurn = Pattern.<SimpleEvent>begin("start")
                .subtype(NaturaEvent.class)
                .where(new SimpleCondition<NaturaEvent>() {

                    @Override
                    public boolean filter(NaturaEvent value) throws Exception {
                        return true;
                    }
                })
                .followedBy("end")
                .subtype(InstantaneousTurnEvent.class)
                .where(new SimpleCondition<InstantaneousTurnEvent>() {

                    @Override
                    public boolean filter(InstantaneousTurnEvent value) throws Exception {
                        return true;
                    }
                });

        CEP.pattern(parsedStream, complexTurn).flatSelect(new PatternFlatSelectFunction<SimpleEvent, String>() {

            @Override
            public void flatSelect(Map<String, List<SimpleEvent>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, List<SimpleEvent>> entry: map.entrySet()) {
                    for (SimpleEvent t: entry.getValue()) {
                        str.append(t.getMmsi());
                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText(outputFile, FileSystem.WriteMode.OVERWRITE);
    }
}
