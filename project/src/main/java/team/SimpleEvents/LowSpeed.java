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
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*        Pattern<DynamicShipClass, DynamicShipClass> gapPattern = Pattern.<DynamicShipClass>begin("startGap")
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return true;
                    }
                })
                .next("end").where(new IterativeCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass previous, Context<DynamicShipClass> ctx) throws Exception {
                        String contex="end";
                        if(!ctx.getEventsForPattern("end").iterator().hasNext())
                        {
                            contex="startGap";
                        }
                        for (DynamicShipClass event : ctx.getEventsForPattern(contex)) {

                            if (previous.ts - event.ts >= 10 * 60*1000)
                                return true;
                            else
                                return false;
                        }
                        return false;
                    }
                });

        DataStream<SimpleEvent> gap = CEP.pattern(parsedStream, gapPattern).
                select((Map<String, List<DynamicShipClass>> pattern) -> {
                    System.out.println("Match Found!");
                    long startTime=pattern.get("startGap").get(0).getTs();
                    long endTime=pattern.get("end").get(0).getTs();
                    DynamicShipClass temp=pattern.get("startGap").get(0);
                    System.out.println("StartTime: "+startTime);
                    System.out.println("EndTime: "+endTime);
                    System.out.println("Duration: "+(endTime-startTime));


                    return new GapEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(),(endTime-startTime));
                });
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        GeoUtils geo = new GeoUtils();
        ArrayList<Integer> naturaArea = geo.latlonToGrid("/home/valia/Desktop/NaturaCentroidsFrance.csv");

        Pattern<DynamicShipClass, DynamicShipClass> naturaAreas = Pattern.<DynamicShipClass>begin("Natura", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        if (naturaArea.contains(value.getGridId()))
                            return true;
                        else
                            return false;
                    }
                }).oneOrMore();


        DataStream<SimpleEvent> natura = CEP.pattern(parsedStream, naturaAreas).
                select((Map<String, List<DynamicShipClass>> pattern) -> {
                    System.out.println("Match Found!");
                    long startTime=pattern.get("Natura").get(0).getTs();
                    long endTime=pattern.get("Natura").get(0).getTs();
                    double lat = pattern.get("Natura").get(0).getLat();
                    double lon = pattern.get("Natura").get(0).getLon();
                    DynamicShipClass temp=pattern.get("Natura").get(0);
                    System.out.println("StartTime: "+startTime);
                    System.out.println("EndTime: "+endTime);
                    System.out.println("Duration: "+(endTime-startTime));


                    return new NaturaEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(),lat, lon);
                });
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Pattern<DynamicShipClass, DynamicShipClass> turnpattern = Pattern.<DynamicShipClass>begin("start")
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed()>0.0; //not including noise
                    }
                })
                .next("end").where(new IterativeCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {

                        for (DynamicShipClass event : ctx.getEventsForPattern("start")) {
                            //calculating heading difference
                            return Math.abs(value.getHeading() - event.getHeading())>15;
                        }
                        return false;
                    }
                });

        DataStream<SimpleEvent> turn =  CEP.pattern(parsedStream, turnpattern)
                .select((Map<String, List<DynamicShipClass>> pattern) -> {
                    long startTime=0;
                    long endTime= 0;
                    int degrees = 0;
                    System.out.println("Match Found!");
                    for (Map.Entry<String, List<DynamicShipClass>> entry: pattern.entrySet()) {
                        startTime= entry.getValue().get(0).getTs();
                        endTime= entry.getValue().get(entry.getValue().size()-1).getTs();
                        degrees = Math.abs(entry.getValue().get(0).getHeading() - entry.getValue().get(entry.getValue().size()-1).getHeading()) ;
                    }
                    DynamicShipClass temp=pattern.get("start").get(0);
                    return new InstantaneousTurnEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(), degrees);
                });

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        DataStream<SimpleEvent> connectedStreams = natura.union(turn, gap, lowspeed);

        //turn.print();
        //connectedStreams.print();
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

        CEP.pattern(connectedStreams, complexTurn).flatSelect(new PatternFlatSelectFunction<SimpleEvent, String>() {

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
        }).writeAsText("output.txt", FileSystem.WriteMode.OVERWRITE);

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Pattern<SimpleEvent, ?> complexLow = Pattern.<SimpleEvent>begin("start")
                .subtype(NaturaEvent.class)
                .where(new SimpleCondition<NaturaEvent>() {

                    @Override
                    public boolean filter(NaturaEvent value) throws Exception {
                        return true;
                    }
                })
                .followedBy("end")
                .subtype(LowSpeedEvent.class)
                .where(new SimpleCondition<LowSpeedEvent>() {

                    @Override
                    public boolean filter(LowSpeedEvent value) throws Exception {
                        return true;
                    }
                });

        CEP.pattern(connectedStreams, complexLow).flatSelect(new PatternFlatSelectFunction<SimpleEvent, String>() {

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
        }).writeAsText("output.txt");

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

        CEP.pattern(connectedStreams, complexGap).flatSelect(new PatternFlatSelectFunction<SimpleEvent, String>() {

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
        }).writeAsText("lowSpeed.txt");


////////////////////////////////////////////////////////////////////////////




*/
