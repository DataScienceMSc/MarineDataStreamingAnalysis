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

import java.util.List;
import java.util.Map;

//* Skeleton for a Flink Streaming Job.
//* <p>For a tutorial how to write a Flink streaming application, check the
//* tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
//*
//* <p>To package your application into a JAR file for execution, run
//* 'mvn clean package' on the command line.
//*
//* <p>If you change the name of the main class (with the public static void main(String[] args))
//* method, change the respective entry in the POM.xml file (simply search for 'mainClass').


public class SpeedChangePattern {

    public static void main(String[] args) throws Exception {

        GeoUtils geo = new GeoUtils();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "/home/valia/Desktop/FarFromPorts.csv";
        TextInputFormat format = new TextInputFormat(
                new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line))
                .keyBy(element -> element.getmmsi());


        Pattern<DynamicShipClass, DynamicShipClass> speedChange = Pattern.<DynamicShipClass>begin("start")
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return true;
                    }
                })
                .next("end").where(new IterativeCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
                        String contex="start";

                        for (DynamicShipClass event : ctx.getEventsForPattern(contex)) {
                            return (Math.abs(value.getSpeed() - event.getSpeed()) > 5.0) &&  (Math.abs(value.getSpeed() - event.getSpeed()) < 70.0);
                        }
                        return false;
                    }
                });

        CEP.pattern(parsedStream, speedChange).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    System.out.println("Match");
                    for (DynamicShipClass t: entry.getValue()) {
                        str.append(t.getmmsi());
                        str.append(",   ");
                        str.append(t.getSpeed());
                        str.append(",   ");
                        str.append(t.getEventTime());
                        str.append(",   ");
                        str.append(t.getLat());
                        str.append(",   ");
                        str.append(t.getLon());
                        //str.append("\n");
                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText("/home/valia/Desktop/speedChange.csv", FileSystem.WriteMode.OVERWRITE);


//        DataStream<SimpleEvent> turn =  CEP.pattern(parsedStream, speedChange)
//                .select((Map<String, List<DynamicShipClass>> pattern) -> {
//                    long startTime=0;
//                    long endTime= 0;
//                    double SpeedChange=0;
//                    System.out.println("Match Found!");
//                    for (Map.Entry<String, List<DynamicShipClass>> entry: pattern.entrySet()) {
//                        startTime= entry.getValue().get(0).getTs();
//                        endTime= entry.getValue().get(entry.getValue().size()-1).getTs();
//                        SpeedChange=Math.abs(entry.getValue().get(0).getSpeed()-
//                            entry.getValue().get(0).getSpeed());
//                    }
//                    DynamicShipClass temp=pattern.get("start").get(0);
//                    return new SpeedChangeEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(), SpeedChange);
//                });

        env.execute();
    }


}