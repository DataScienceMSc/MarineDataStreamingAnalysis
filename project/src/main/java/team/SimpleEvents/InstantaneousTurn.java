package team.SimpleEvents;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import team.General.DynamicShipClass;

import java.util.List;
import java.util.Map;



public class InstantaneousTurn {

    public InstantaneousTurn(){};

    public static DataStream<SimpleEvent> generateSimpleEvents(DataStream<DynamicShipClass> parsedStream) throws Exception {

        //do we need that?
        //GeoUtils geo = new GeoUtils();

        Pattern<DynamicShipClass, DynamicShipClass> turnPattern = Pattern.<DynamicShipClass>begin("start")
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed() > 0.0; //not including noise
                    }
                })
                .next("end").where(new IterativeCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {

                        for (DynamicShipClass event : ctx.getEventsForPattern("start")) {
                            //calculating heading difference
                            return Math.abs(value.getHeading() - event.getHeading()) > 15;
                        }
                        return false;
                    }
                });


        DataStream<SimpleEvent> turn = CEP.pattern(parsedStream, turnPattern)
                .select((Map<String, List<DynamicShipClass>> pattern) -> {
                    long startTime = 0;
                    long endTime = 0;
                    int degrees = 0;
                    System.out.println("Match Found!");
                    for (Map.Entry<String, List<DynamicShipClass>> entry : pattern.entrySet()) {
                        startTime = entry.getValue().get(0).getTs();
                        endTime = entry.getValue().get(entry.getValue().size() - 1).getTs();
                        degrees = Math.abs(entry.getValue().get(0).getHeading() - entry.getValue().get(entry.getValue().size() - 1).getHeading());
                    }
                    DynamicShipClass temp = pattern.get("start").get(0);
                    return new InstantaneousTurnEvent(temp.getmmsi(), startTime, endTime, temp.getGridId(), degrees);
                });

        return turn;
    }

}