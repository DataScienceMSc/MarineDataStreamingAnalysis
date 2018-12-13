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


public class Gaps {

    public static void main(String[] args) throws Exception {

        GeoUtils geo = new GeoUtils();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "folder/";
        TextInputFormat format = new TextInputFormat(
                new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line))
                .keyBy(element -> element.getmmsi());


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

                                if (previous.ts - event.ts >= 10 * 60*1000)
                                    return true;
                                else
                                    return false;
                        }
                        return false;
                    }
                });

        DataStream<SimpleEvent> gapEvent = CEP.pattern(parsedStream, increasingSpeed).
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

        env.execute();



    }


}