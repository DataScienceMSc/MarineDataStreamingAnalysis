package team.SimpleEvents;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import team.General.DynamicShipClass;
import team.General.GeoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FarFromPorts implements Runnable{


    static DataStream<String> inputStream;
    static String outputFile;
    static StreamExecutionEnvironment env;

    public FarFromPorts(DataStream<String> stream, String outputFile, StreamExecutionEnvironment env) {
        this.inputStream = stream;
        this.outputFile = outputFile;
        this.env=env;

    }


    public  void run(){
        try {

            GeoUtils geo = new GeoUtils();

            DataStream<DynamicShipClass> parsedStream = inputStream
                    .map(line -> DynamicShipClass.fromString(line,geo))
                    .keyBy(DynamicShipClass::getmmsi);

            ArrayList<Integer> portsOfBrittany = geo.latlonToGrid("./inputFiles/latlon.csv");

            Pattern<DynamicShipClass, DynamicShipClass> farFromPorts = Pattern.<DynamicShipClass>begin("openSea", AfterMatchSkipStrategy.skipPastLastEvent())
                    .where(new SimpleCondition<DynamicShipClass>() {

                        @Override
                        public boolean filter(DynamicShipClass value) throws Exception {
                            return (!portsOfBrittany.contains(value.getGridId()));
                        }
                    });

            CEP.pattern(parsedStream, farFromPorts).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {

                @Override
                public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                    StringBuilder str = new StringBuilder();
                    for (Map.Entry<String, List<DynamicShipClass>> entry : map.entrySet()) {
                        System.out.println("farFromPorts,");
                        for (DynamicShipClass t : entry.getValue()) {
                            str.append("farFromPorts,");
                            str.append(t.getmmsi());
                            str.append(",");
                            str.append(t.getStatus());
                            str.append(",");
                            str.append(t.getTurn());
                            str.append(",");
                            str.append(t.getSpeed());
                            str.append(",");
                            str.append(t.getCourse());
                            str.append(",");
                            str.append(t.getHeading());
                            str.append(",");
                            str.append(t.getLon());
                            str.append(",");
                            str.append(t.getLat());
                            str.append(",");
                            str.append(t.getTs());
                            str.append(",");
                            str.append(t.getGridId());
//                        str.append("\n");
                        }
                    }
                    collector.collect(str.toString());
                }
            }).writeAsText(outputFile, FileSystem.WriteMode.OVERWRITE);
            env.execute();
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
    }
}

