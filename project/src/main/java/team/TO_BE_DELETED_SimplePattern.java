package team;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.util.Collector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TO_BE_DELETED_SimplePattern {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //String path = "folder/";
        String path = "/home/valia/MarineDataStreamingAnalysis/project/folder/ais_data_very_very_small.csv";
        TextInputFormat format = new TextInputFormat(
                new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line))
                .keyBy(element -> element.getmmsi());


        Pattern<DynamicShipClass, DynamicShipClass> movingShip = Pattern.<DynamicShipClass>begin("stoppedBefore")
                .where(new SimpleCondition<DynamicShipClass>() {
                    //private static final long serialVersionUID = 314415972814127035L;

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return value.getSpeed()==0.0;
                    }
                })
                .followedBy("middle")
                .where(new IterativeCondition<DynamicShipClass>() {
                    //private static final long serialVersionUID = 6664468385615273240L;

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
                        return value.getSpeed() > 0.0;
                    }
                }).times(2)
                .followedBy("end").where(new IterativeCondition<DynamicShipClass>() {
                    //private static final long serialVersionUID = 6664468385615273240L;

                    @Override
                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
                        return value.getSpeed() == 0.0;
                    }
                });


        CEP.pattern(parsedStream, movingShip).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {

            @Override
            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
                StringBuilder str = new StringBuilder();
                System.out.println("here");
                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
                    for (DynamicShipClass t: entry.getValue()) {
                        str.append(t.getmmsi());
                        str.append(",");
                        str.append(t.getSpeed());
                        str.append("\n");
                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText("output.txt", FileSystem.WriteMode.OVERWRITE);

        env.execute();


    }


}