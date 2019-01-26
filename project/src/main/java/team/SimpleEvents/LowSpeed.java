package team.SimpleEvents;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import team.General.DynamicShipClass;

import java.util.List;
import java.util.Map;

public class LowSpeed {

    public LowSpeed(){};


    public static DataStream<SimpleEvent> generateSimpleEvents(DataStream<DynamicShipClass> parsedStream) throws Exception {


        Pattern<DynamicShipClass, ?> low = Pattern.<DynamicShipClass>begin("lowStart"
                , AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {
                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed()>=0.5 && value.getSpeed()<5.0;
                    }
                })
                .oneOrMore().greedy().consecutive()
                .next("lowEnd")
                .where(new IterativeCondition<DynamicShipClass>() {
                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
                        return value.getSpeed()<0.5 || value.getSpeed()>5.0;
                    }
                });

        DataStream<SimpleEvent> lowspeed = CEP.pattern(parsedStream, low).
                select((Map<String, List<DynamicShipClass>> pattern) -> {
                    long startTime=0;
                    long endTime= 0;
                    double speed = 0.0;
                    int counter = 0;
                    System.out.println("Match");
                    for (Map.Entry<String, List<DynamicShipClass>> entry: pattern.entrySet()) {
                        for(DynamicShipClass t: entry.getValue()) {
                            speed = speed + t.getSpeed();
                            counter +=1;
                        }
                        startTime= entry.getValue().get(0).getTs();
                        endTime= entry.getValue().get(entry.getValue().size()-1).getTs();
                    }
                    speed = speed/counter;
                    DynamicShipClass temp=pattern.get("lowStart").get(0);
                    return new LowSpeedEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(),speed);
                });
        return lowspeed;

    }
}


