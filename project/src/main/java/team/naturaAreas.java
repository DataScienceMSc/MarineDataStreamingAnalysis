package team;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.util.Collector;
import team.General.DynamicShipClass;
import team.General.GeoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class naturaAreas {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        GeoUtils geo = new GeoUtils();
        ArrayList<Integer> naturaArea = geo.latlonToGrid("/home/valia/Desktop/NaturaCentroidsFrance.csv");

        String path = "/home/valia/Desktop/FarFromPorts.csv";
        TextInputFormat format = new TextInputFormat(
                new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line))
                .keyBy(element -> element.getmmsi());

        Pattern<DynamicShipClass, DynamicShipClass> naturaAreas = Pattern.<DynamicShipClass>begin("Natura", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        if (naturaArea.contains(value.getGridId())) {
                            System.out.println("Grid Id:" + value.getGridId());
                            return true;
                        }
                        else
                            return false;
                    }
                }).oneOrMore();


        CEP.pattern(parsedStream, naturaAreas).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {


            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                Integer counter=0;
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    System.out.println("Match");

                    for (DynamicShipClass t: entry.getValue()) {
                        if (counter == 0) {
                            str.append(t.getmmsi());
                            counter = counter + 1;
                        }
                        str.append("," + t.getLat());
                        str.append("," + t.getLon());
                        str.append(", " + t.getTs());

                    }
                }
                //str.append("\n");
                collector.collect(str.toString());
            }
        }).writeAsText("./results/naturaAreas.txt", FileSystem.WriteMode.OVERWRITE);

        env.execute();

    }
}