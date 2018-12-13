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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class naturaAreas {

    public static void main(String[] args) throws Exception {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        GeoUtils geo = new GeoUtils();
        ArrayList<Integer> naturaArea = geo.latlonToGrid("/home/valia/Desktop/NaturaCentroidsFrance.csv");

        String path = "/home/valia/MarineDataStreamingAnalysis/project/folder/ais_data_small.csv";
        TextInputFormat format = new TextInputFormat(new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line, geo))
                .keyBy(element -> element.getmmsi());

        Pattern<DynamicShipClass, DynamicShipClass> increasingSpeed = Pattern.<DynamicShipClass>begin("Natura", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        if (naturaArea.contains(value.getGridId()) || naturaArea.contains(value.getGridId()-1) || naturaArea.contains(value.getGridId() + 1))
                            return true;
                        else
                            return false;
                    }
                });


        CEP.pattern(parsedStream, increasingSpeed).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {
            private static final long serialVersionUID = -8972838879934875538L;

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, List<DynamicShipClass>> entry : map.entrySet()) {
                    System.out.println("Match");
                    for (DynamicShipClass t : entry.getValue()) {
                        str.append(t.getmmsi());

                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText("/home/valia/MarineDataStreamingAnalysis/project/folder/output.txt", FileSystem.WriteMode.OVERWRITE);

        env.execute();

    }
}