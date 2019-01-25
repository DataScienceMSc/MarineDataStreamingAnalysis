package team;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TO_BE_DELETED_Preprocessing {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        GeoUtils geo = new GeoUtils();
        String path = "/home/valia/MarineDataStreamingAnalysis/project/folder/ais_data_small.csv";
        TextInputFormat format = new TextInputFormat(
                new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line))
                .keyBy(element -> element.getmmsi());


        Pattern<DynamicShipClass, DynamicShipClass> stoppedShip = Pattern.<DynamicShipClass>begin("start"
                , AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed()<0.5;
                    }
                })
                .oneOrMore().greedy().consecutive()
                .next("end")
                .where(new IterativeCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
                        return value.getSpeed() > 0.5;
                    }
                });

        ArrayList<PreprocessedEvents> StoppedEvents = new ArrayList<>();
        CEP.pattern(parsedStream, stoppedShip).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {
            private static final long serialVersionUID = -8972838879934875538L;

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    System.out.println("Match");
                    PreprocessedEvents event = new PreprocessedEvents();
                    for (DynamicShipClass t: entry.getValue()) {
                        event.setmmsi(t.getmmsi());
                        event.setIsStopped((true));
                        event.setSpeed(t.getSpeed());
                        event.setgapEnd(false);
                        event.setgapStart(false);
                        event.setTurn(false);
                        event.setTs(t.getTs());
                        event.setLowEnd(false);
                        event.setLowStart(false);
                        event.setGridId(event.assignGeo(t, geo));
                        str.append(t.getmmsi());
                        str.append(",");
                        str.append(t.getSpeed());
                        str.append(",");
                        str.append(t.getEventTime());
                        str.append("\n");
                    }
                    StoppedEvents.add(event);
                }
                collector.collect(str.toString());

            }
        })/*writeAsText("output.txt", FileSystem.WriteMode.OVERWRITE)*/;


        /***************************************************************************************************/

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
                        String contex="end";
                        if(!ctx.getEventsForPattern("end").iterator().hasNext())
                        {
                            contex="startGap";
                        }
                        for (DynamicShipClass event : ctx.getEventsForPattern(contex)) {

                            if (previous.ts - event.ts >= 10 * 60)
                            {
                                //System.out.println("here");
                                previous.setgapStart(true);
                                previous.setgapEnd(false);
                                event.setgapStart(false);
                                event.setgapEnd(true);
                                //System.out.println(previous.gapStart + " " + previous.gapEnd + " " + event.gapStart + " " + event.gapEnd);
                                //System.out.println(previous.ts + " " +event.ts);
                                return true;
                            }
                            else
                                return false;
                        }
                        return false;
                    }
                });
        ArrayList<PreprocessedEvents> GapEvents = new ArrayList<>();
        CEP.pattern(parsedStream, increasingSpeed).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {
            private static final long serialVersionUID = -8972838879934875538L;

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    PreprocessedEvents event = new PreprocessedEvents();
                    for (DynamicShipClass t: entry.getValue()) {
                        event.setmmsi(t.getmmsi());
                        event.setIsStopped((false));
                        event.setSpeed(t.getSpeed());
                        event.setgapEnd(t.getgapEnd());
                        event.setgapStart(t.getgapStart());
                        event.setTurn(false);
                        event.setTs(t.getTs());
                        event.setLowEnd(false);
                        event.setLowStart(false);
                        event.setGridId(event.assignGeo(t, geo));
                        str.append(t.getmmsi());
                        str.append(",   ");
                        str.append(t.getTs());
                        str.append(",   ");
                        str.append(t.getgapStart());
                        str.append(",   ");
                        str.append(t.getgapEnd());
                        str.append("\n");
                    }
                    GapEvents.add(event);
                }
                collector.collect(str.toString());
            }
        })/*.writeAsText("/home/valia/MarineDataStreamingAnalysis/project/folder/output.txt", FileSystem.WriteMode.OVERWRITE)*/;


        /***************************************************************************************************/

        Pattern<DynamicShipClass, DynamicShipClass> movingShip = Pattern.<DynamicShipClass>begin("lowStart", AfterMatchSkipStrategy.skipPastLastEvent())

                .where(new SimpleCondition<DynamicShipClass>() {
                    //private static final long serialVersionUID = 314415972814127035L;

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        if (value.getSpeed()>=0.5 && value.getSpeed()<5.0){
                            value.setLowStart(true);
                            return true;
                        }
                        else
                            return false;
                    }
                })
                .next("middle")
                .where(new SimpleCondition<DynamicShipClass>() {
                    //private static final long serialVersionUID = 314415972814127035L;

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
                        if (value.getSpeed()<0.5 || value.getSpeed()>5.0){
                            value.setLowEnd(true);
                            return true;
                        }
                        else
                            return false;
                    }
                });

        ArrayList<PreprocessedEvents> LowSpeedEvents = new ArrayList<>();
        CEP.pattern(parsedStream, movingShip).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {
            private static final long serialVersionUID = -8972838879934875538L;

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                //System.out.println("here");
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    PreprocessedEvents event = new PreprocessedEvents();
                    for (DynamicShipClass t: entry.getValue()) {
                        event.setmmsi(t.getmmsi());
                        event.setIsStopped((false));
                        event.setSpeed(t.getSpeed());
                        event.setgapEnd(false);
                        event.setgapStart(false);
                        event.setTurn(false);
                        event.setTs(t.getTs());
                        event.setLowEnd(t.getLowEnd());
                        event.setLowStart(t.getLowStart());
                        event.setGridId(event.assignGeo(t, geo));
                        str.append(t.getmmsi());
                        str.append(",");
                        str.append(t.getSpeed());
                        str.append("\n");
                    }
                    LowSpeedEvents.add(event);
                }
                collector.collect(str.toString());
            }
        }).writeAsText("/home/valia/MarineDataStreamingAnalysis/project/folder/output.txt", FileSystem.WriteMode.OVERWRITE);


        /***************************************************************************************************/

        Pattern<DynamicShipClass, DynamicShipClass> instaneousTurn = Pattern.<DynamicShipClass>begin("start")
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
                            return Math.abs(value.getHeading() -event.getHeading())>45;
                        }
                        return false;
                    }
                });

        ArrayList<PreprocessedEvents> TurnEvents = new ArrayList<>();
        CEP.pattern(parsedStream, increasingSpeed).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {
            private static final long serialVersionUID = -8972838879934875538L;

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                Integer counter=0;
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    //System.out.println("Match");
                    PreprocessedEvents event = new PreprocessedEvents();
                    for (DynamicShipClass t: entry.getValue()) {
                        event.setmmsi(t.getmmsi());
                        event.setIsStopped((false));
                        event.setSpeed(t.getSpeed());
                        event.setgapEnd(false);
                        event.setgapStart(false);
                        event.setTurn(true);
                        event.setTs(t.getTs());
                        event.setLowEnd(false);
                        event.setLowStart(false);
                        event.setGridId(event.assignGeo(t, geo));
                        if(counter==0) {
                            str.append("Instantaneous turn for MMSI:" + t.getmmsi()+"{");
                        }
                        str.append(", Heading"+counter.toString()+": "+t.getHeading());
                        counter = counter + 1;
                    }
                    TurnEvents.add(event);
                }str.append("}\n");
                collector.collect(str.toString());
            }
        })/*.writeAsText("output.txt", FileSystem.WriteMode.OVERWRITE)*/;


        ArrayList<PreprocessedEvents> FinalEvents = new ArrayList<>();

        // TODO: 13/12/2018 join all the arrayLists 
        env.execute();


    }


}