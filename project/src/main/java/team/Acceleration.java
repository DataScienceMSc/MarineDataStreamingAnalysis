package team;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;


public class Acceleration {

    public static void main(String[] args) throws Exception {

        GeoUtils geo = new GeoUtils();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "/Users/thanasiskaridis/Desktop/FarFromPorts.csv";
        TextInputFormat format = new TextInputFormat(
                new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line))
                .keyBy(element -> element.getmmsi());


        Pattern<DynamicShipClass, DynamicShipClass> increasingSpeed = Pattern.<DynamicShipClass>begin("start")
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

        CEP.pattern(parsedStream, increasingSpeed).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {

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
                        //str.append(", Speed"+counter.toString()+": "+t.getSpeed());
                        str.append("," + t.getLat());
                        str.append("," + t.getLon());
                        //str.append(", timestamp " + t.getTs());
                    }

                }str.append("\n");
                collector.collect(str.toString());
            }
            }).writeAsText("/Users/thanasiskaridis/Desktop/acceleration.txt", FileSystem.WriteMode.OVERWRITE);

        env.execute();
        System.out.println("end of matches");

    }


}