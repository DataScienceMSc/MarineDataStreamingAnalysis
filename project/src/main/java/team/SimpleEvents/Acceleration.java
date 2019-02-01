package team.SimpleEvents;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
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


public class Acceleration implements Runnable{


    static DataStream<String> inputStream;
    static String outputFile;
    static StreamExecutionEnvironment env;

    public Acceleration(DataStream<String> stream, String outputFile, StreamExecutionEnvironment env) {
        this.inputStream = stream;
        this.outputFile = outputFile;
        this.env=env;

    }


    public void run(){
        try{
            DataStream<DynamicShipClass> parsedStream = inputStream
                    .map(line -> DynamicShipClass.fromString(line)).keyBy(DynamicShipClass::getmmsi);

               Pattern<DynamicShipClass, DynamicShipClass> acc = Pattern.<DynamicShipClass>begin("start")
                    .where(new SimpleCondition<DynamicShipClass>() {

                        @Override
                        public boolean filter(DynamicShipClass value) throws Exception {
                            return value.getSpeed()>0.1; //not including noise
                            }
                    })
                   .next("end").where(new IterativeCondition<DynamicShipClass>() {

                        @Override
                        public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {

                            for (DynamicShipClass event : ctx.getEventsForPattern("start")) {
                                //calculating percentage increase
                                Double acc = ((value.getSpeed() - event.getSpeed())/(value.getTs()-event.getTs()));
                                if (acc >= 0.25)
                                    return true;
                            }
                            return false;
                        }
                    });

            CEP.pattern(parsedStream, acc).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {

                @Override
                public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                    StringBuilder str = new StringBuilder();
                    Integer counter=0;
                    for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                        System.out.println("Match");

                        for (DynamicShipClass t: entry.getValue()) {
                            if(counter==0) {
                                str.append(t.getmmsi());
                                counter = counter + 1;
                            }
                            str.append("," + t.getLat());
                            str.append("," + t.getLon());
                        }

                    }str.append("\n");
                    collector.collect(str.toString());
                }
                }).writeAsText(outputFile, FileSystem.WriteMode.OVERWRITE);
                env.execute();

            }catch(Exception e){
                System.out.println(e.getCause());
            }
    }


}