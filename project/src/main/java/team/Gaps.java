package team;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;

import java.util.List;
import java.util.Map;


public class Gaps {

    Gaps() {}

    static DataStream<SimpleEvent> generateSimpleEvents(DataStream<DynamicShipClass> parsedStream) throws Exception {


        Pattern<DynamicShipClass, DynamicShipClass> increasingSpeed = Pattern.<DynamicShipClass>begin("startGap")
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

                            if (previous.ts - event.ts >= 10 * 60 * 2)
                                return true;
                            else
                                return false;
                        }
                        return false;
                    }
                });

        DataStream<SimpleEvent> gapStream = CEP.pattern(parsedStream, increasingSpeed).select((Map<String, List<DynamicShipClass>> pattern) -> {
            System.out.println("Match Found!");
            long startTime=pattern.get("startGap").get(0).getTs();
            long endTime=pattern.get("end").get(0).getTs();
            DynamicShipClass temp=pattern.get("startGap").get(0);
            return new GapEvent(temp.getmmsi(), startTime, endTime, temp.getGridId(), (startTime - endTime));

        });
        return gapStream;
    }
}
