package team.SimpleEvents;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import team.General.DynamicShipClass;

import java.util.List;
import java.util.Map;

public class StoppedAfterMoving {

    public StoppedAfterMoving() {};

    public static DataStream<SimpleEvent> generateSimpleEvents(DataStream<DynamicShipClass> parsedStream) throws Exception {

        Pattern<DynamicShipClass, ?> Stopped = Pattern.<DynamicShipClass>begin("start",
                AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed() > 5.0;
                    }
                })
                .oneOrMore().greedy().consecutive()
                .next("end")
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed() < 0.5;
                    }
                });

        DataStream<SimpleEvent> stoppedShips = CEP.pattern(parsedStream, Stopped).
                select((Map<String, List<DynamicShipClass>> pattern) -> {
                    long startTime = 0;
                    long endTime = 0;
                    System.out.println("Stopped Ship");
                    for (Map.Entry<String, List<DynamicShipClass>> entry : pattern.entrySet()) {
                        startTime = entry.getValue().get(0).getTs();
                        endTime = entry.getValue().get(entry.getValue().size() - 1).getTs();
                    }
                    DynamicShipClass temp = pattern.get("start").get(0);
                    return new StoppedEvent(temp.getmmsi(), startTime, endTime, temp.getGridId(),
                            temp.getSpeed(),temp.getLat(), temp.getLon());
                });

        return stoppedShips;

    }
}
