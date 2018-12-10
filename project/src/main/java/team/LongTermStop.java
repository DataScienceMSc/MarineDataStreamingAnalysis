//package team;
//
//import org.apache.flink.api.common.state.ValueState;
//import org.apache.flink.api.java.io.TextInputFormat;
//import org.apache.flink.cep.CEP;
//import org.apache.flink.cep.PatternFlatSelectFunction;
//import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
//import org.apache.flink.cep.pattern.Pattern;
//import org.apache.flink.cep.pattern.conditions.IterativeCondition;
//import org.apache.flink.cep.pattern.conditions.SimpleCondition;
//import org.apache.flink.core.fs.FileSystem;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
//import org.apache.flink.types.Value;
//import org.apache.flink.util.Collector;
//
//import java.util.List;
//import java.util.Map;
//
////* Skeleton for a Flink Streaming Job.
////* <p>For a tutorial how to write a Flink streaming application, check the
////* tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
////*
////* <p>To package your application into a JAR file for execution, run
////* 'mvn clean package' on the command line.
////*
////* <p>If you change the name of the main class (with the public static void main(String[] args))
////* method, change the respective entry in the POM.xml file (simply search for 'mainClass').
//
//
//public class LongTermStop {
//
//    public static void main(String[] args) throws Exception {
//
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setParallelism(1);
//
//        //String path = "folder/";
//        String path = "/home/valia/MarineDataStreamingAnalysis/project/folder/ais_data_small.csv";
//        TextInputFormat format = new TextInputFormat(
//                new org.apache.flink.core.fs.Path(path));
//        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);
//
//        DataStream<DynamicShipClass> parsedStream = inputStream
//                .map(line -> DynamicShipClass.fromString(line))
//                .keyBy(element -> element.getmmsi(), ValueState<>);
//
//
//        Pattern<DynamicShipClass, DynamicShipClass> movingShip = Pattern.<DynamicShipClass>begin("LongStopStart")
//
//                .where(new SimpleCondition<DynamicShipClass>() {
//                    //private static final long serialVersionUID = 314415972814127035L;
//
//                    @Override
//                    public boolean filter(DynamicShipClass value) throws Exception {
//                        double radLon = Math.toRadians(value.lon);
//                        double radLat = Math.toRadians(value.lat);
//                        //return value.getSpeed()>=2.7 && value.getSpeed()<50.;
//                    }
//                })
//                .followedBy("middle")
//                .where(new SimpleCondition<DynamicShipClass>() {
//                    //private static final long serialVersionUID = 314415972814127035L;
//
//                    @Override
//                    public boolean filter(DynamicShipClass value) throws Exception {
//                        double radLon1 = Math.toRadians(value.lon);
//                        double radLat1 = Math.toRadians(value.lat);
//                        double distance = CoordDistance(radLon1, )
//                        return value.getSpeed()>=2.7 && value.getSpeed()<50.;
//                    }
//                })
//                ;
//
//
//
//
//        CEP.pattern(parsedStream, movingShip).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {
//            private static final long serialVersionUID = -8972838879934875538L;
//
//            @Override
//            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
//                StringBuilder str = new StringBuilder();
//                //System.out.println("here");
//                for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
//                    for (DynamicShipClass t: entry.getValue()) {
//                        str.append(t.getmmsi());
//                        str.append(",");
//                        str.append(t.getSpeed());
//                        str.append("\n");
//                    }
//                }
//                collector.collect(str.toString());
//            }
//        }).writeAsText("/home/valia/MarineDataStreamingAnalysis/project/folder/output.txt", FileSystem.WriteMode.OVERWRITE);
//        //.writeAsText("output.txt", FileSystem.WriteMode.OVERWRITE);
//        env.execute();
//
//
//    }
//    double CoordDistance(double latitude1, double longitude1, double latitude2, double longitude2)
//    {
//        return 6371 * Math.acos(
//                Math.sin(latitude1) * Math.sin(latitude2)
//                        + Math.cos(latitude1) * Math.cos(latitude2) * Math.cos(longitude2 - longitude1));
//    }
//
//}