package team.SimpleEvents;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.util.Collector;
import team.General.DynamicShipClass;

import java.util.List;
import java.util.Map;

public class RendezVousSimple implements Runnable{


        static DataStream<String> inputStream;
        static String outputFile;
        static StreamExecutionEnvironment env;

        public RendezVousSimple(DataStream<String> stream, String outputFile, StreamExecutionEnvironment env) {
            this.inputStream = stream;
            this.outputFile = outputFile;
            this.env=env;

        }


        public void run(){
            try{
                DataStream<DynamicShipClass> parsedStream = inputStream
                        .map(line -> DynamicShipClass.fromString(line))
                        .keyBy(DynamicShipClass::getGridId);

        Pattern<DynamicShipClass, DynamicShipClass> stoppedShip = Pattern.<DynamicShipClass>begin("start", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed() < 0.5;
                    }
                }).oneOrMore().consecutive().next("end").where(new IterativeCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
                        String contex="end";
                        if(!ctx.getEventsForPattern("end").iterator().hasNext())
                        {
                            contex="start";
                        }
                        for (DynamicShipClass event : ctx.getEventsForPattern(contex)) {
                                return ((event.getTs() - value.getTs() < 10 * 60) && (event.getmmsi()
                                        != value.getmmsi()) && (value.getSpeed()<0.5));
                        }
                        return false;
                    }
                });



        CEP.pattern(parsedStream, stoppedShip).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    System.out.println("Rendez Vouz,");
                    for (DynamicShipClass t: entry.getValue()) {
                        str.append("Rendez Vouz,");
                        str.append(t.getmmsi());
                        str.append(",   ");
                        str.append(t.getSpeed());
                        str.append(",   ");
                        str.append(t.getEventTime());
                        str.append(",   ");
                        str.append(t.getGridId());
                        str.append(",   ");
                        str.append(t.getLat());
                        str.append(",   ");
                        str.append(t.getLon());
                        str.append("\n");
                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText(outputFile, FileSystem.WriteMode.OVERWRITE);
                env.execute();

            }catch(Exception e){
                System.out.println(e.getCause());
            }
        }


}