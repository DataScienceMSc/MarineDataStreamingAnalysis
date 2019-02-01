package team.SimpleEvents;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import team.General.DynamicShipClass;

import java.util.List;
import java.util.Map;


public class Gaps {

    public Gaps() {}

    public static DataStream<SimpleEvent> generateSimpleEvents(DataStream<DynamicShipClass> parsedStream) throws Exception {


        Pattern<DynamicShipClass, DynamicShipClass> gap = Pattern.<DynamicShipClass>begin("startGap")
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return true;
                    }
                })
                .next("end").where(new IterativeCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass previous, Context<DynamicShipClass> ctx) throws Exception {
                        String contex = "end";
                        if (!ctx.getEventsForPattern("end").iterator().hasNext()) {
                            contex = "startGap";
                        }
                        for (DynamicShipClass event : ctx.getEventsForPattern(contex)) {

                            return (previous.ts - event.ts >= 10 * 60 * 2);
                        }
                        return false;
                    }
                });

        DataStream<SimpleEvent> gapStream = CEP.pattern(parsedStream, gap).select((Map<String, List<DynamicShipClass>> pattern) -> {
            System.out.println("Gap");
            long startTime=pattern.get("startGap").get(0).getTs();
            long endTime=pattern.get("end").get(0).getTs();
            DynamicShipClass temp=pattern.get("startGap").get(0);
            return new GapEvent(temp.getmmsi(), startTime, endTime, temp.getGridId(), (startTime - endTime));

        });
        return gapStream;
    }
}
