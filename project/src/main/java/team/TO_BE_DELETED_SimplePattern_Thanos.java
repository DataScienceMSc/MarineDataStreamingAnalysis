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

//* Skeleton for a Flink Streaming Job.
//* <p>For a tutorial how to write a Flink streaming application, check the
//* tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
//*
//* <p>To package your application into a JAR file for execution, run
//* 'mvn clean package' on the command line.
//*
//* <p>If you change the name of the main class (with the public static void main(String[] args))
//* method, change the respective entry in the POM.xml file (simply search for 'mainClass').


public class TO_BE_DELETED_SimplePattern_Thanos {

    public static void main(String[] args) throws Exception {



//        GeoUtils geo = new GeoUtils();
//        int gridId = geo.mapToGridCell((float) -4.4851017, (float) 48.38134);
//        System.out.printf("gridId: "+ gridId);
//        ArrayList<Integer> portsOfBrittany = geo.latlonToGrid("/Users/thanasiskaridis/Desktop/latlon.csv");
//        if (portsOfBrittany.contains(gridId)){
//            System.out.printf("true");
//        }else{
//            System.out.printf("false");
//        }


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        GeoUtils geo = new GeoUtils();
        ArrayList<Integer> portsOfBrittany = geo.latlonToGrid("/Users/thanasiskaridis/Desktop/latlon.csv");

        String path = "/Users/thanasiskaridis/Desktop/maritime/MarineDataStreamingAnalysis/project/ais_data_small.csv";
        TextInputFormat format = new TextInputFormat(
                new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream = inputStream
                .map(line -> DynamicShipClass.fromString(line,geo))
                .keyBy(element -> element.getmmsi());

        Pattern<DynamicShipClass, DynamicShipClass> increasingSpeed = Pattern.<DynamicShipClass>begin("startSlowMotion", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        //System.out.println("first event");
                        if (!portsOfBrittany.contains(value.getGridId()))
                        {
                            System.out.println("The ship's location is not a port");
                            return true;
                        }
                        else
                            return false;
                    }
                });



        CEP.pattern(parsedStream, increasingSpeed).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {
            private static final long serialVersionUID = -8972838879934875538L;

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
                        str.append(t.getHeading());
                        str.append(",   ");
                        str.append(t.getGridId());
                        str.append(",   ");
                        str.append(t.getStatus());
                        str.append(",   ");
                        str.append(t.getTurn());
                        str.append(",   ");
                        str.append(t.getCourse());
                        str.append(",   ");
                        str.append(t.getLat());
                        str.append(",   ");
                        str.append(t.getLon());
                        str.append(",   ");
                        str.append(t.getTs());
                        str.append("\n");
                    }
                }
                collector.collect(str.toString());
            }
        }).writeAsText("/Users/thanasiskaridis/Desktop//ShipsNotAtPort.txt", FileSystem.WriteMode.OVERWRITE);

        env.execute();

    }

}