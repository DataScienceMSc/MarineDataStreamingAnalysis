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

//* Skeleton for a Flink Streaming Job.
//* <p>For a tutorial how to write a Flink streaming application, check the
//* tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
//*
//* <p>To package your application into a JAR file for execution, run
//* 'mvn clean package' on the command line.
//*
//* <p>If you change the name of the main class (with the public static void main(String[] args))
//* method, change the respective entry in the POM.xml file (simply search for 'mainClass').


public class SimplePattern_Thanos {

    public static void main(String[] args) throws Exception {


        GeoUtils geo = new GeoUtils();
        String lineTest = 1 + "," + 2 + "," + 3 + "," + 4 + "," + 5 + "," + 6 + "," + 1 + "," + 2+ "," + 1;
        String lineTest2 = 1 + "," + 2 + "," + 3 + "," + 4 + "," + 5 + "," + 6 + "," + 3 + "," + 4+ "," + 2;

        DynamicShipClass test = DynamicShipClass.fromString(lineTest, geo);
        DynamicShipClass test2 = DynamicShipClass.fromString(lineTest2, geo);
        Velocity vel = new Velocity(test, test2);
        int heading = vel.velHeading;
        double eucl = vel.velValue;
        System.out.printf("heading: "+ heading + "eucl: "+ eucl);
      /*
    int gridId = geo.mapToGridCell((float) -7.43435, (float) 43.43438);
    int gridId2 = geo.mapToGridCell((float) -7.43334, (float) 43.43334);
    System.out.println("id1 " + gridId + " id2 " + gridId2);
    double dist = geo.getDistance(43.43434, -7.43434, 43.43334, -7.43334);
    System.out.println("distance " + dist); */

      //test for GridId

//        String lineTest = 1 + "," + 2 + "," + 3 + "," + 4 + "," + 5 + "," + 6 + "," + -7.43435 + "," + 43.43438+ "," + 123456789;
//        DynamicShipClass test = DynamicShipClass.fromString(lineTest, geo);
//        int grid_id = test.getGridId();
//        System.out.printf(String.valueOf(grid_id));
//
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setParallelism(1);
//
//        String path = "/Users/thanasiskaridis/Desktop/maritime/MarineDataStreamingAnalysis/project/folder";
//        TextInputFormat format = new TextInputFormat(
//                new org.apache.flink.core.fs.Path(path));
//        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);
//
//        DataStream<DynamicShipClass> parsedStream = inputStream
//                .map(line -> DynamicShipClass.fromString(line, geo))
//                .keyBy(element -> element.getmmsi());
//
//
//        Pattern<DynamicShipClass, DynamicShipClass> movingShip = Pattern.<DynamicShipClass>begin("stoppedBefore")
//                .where(new SimpleCondition<DynamicShipClass>() {
//                    //private static final long serialVersionUID = 314415972814127035L;
//
//                    @Override
//                    public boolean filter(DynamicShipClass value) throws Exception {
//                        return value.getSpeed()==0.0;
//                    }
//                })
//                .followedBy("middle")
//                .where(new IterativeCondition<DynamicShipClass>() {
//                    //private static final long serialVersionUID = 6664468385615273240L;
//
//                    @Override
//                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
//                        return value.getSpeed() > 0.0;
//                    }
//                }).times(2)
//                .followedBy("end").where(new IterativeCondition<DynamicShipClass>() {
//                    //private static final long serialVersionUID = 6664468385615273240L;
//
//                    @Override
//                    public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
//                        return value.getSpeed() == 0.0;
//                    }
//                });
//
//
//        CEP.pattern(parsedStream, movingShip).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {
//            //private static final long serialVersionUID = -8972838879934875538L;
//
//            @Override
//            public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
//                StringBuilder str = new StringBuilder();
//                System.out.println("here");
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
//        }).writeAsText("output.txt", FileSystem.WriteMode.OVERWRITE);
//
//        env.execute();
//

    }

    //EuclideanDistance for Valia

//    public static float DEG_LEN = 110.25f;
//
//    public static double getEuclideanDistance(float lon1, float lat1, float lon2, float lat2) {
//        double x = lat1 - lat2;
//        double y = (lon1 - lon2) * Math.cos(lat2);
//        return (DEG_LEN * Math.sqrt(x*x + y*y));
//    }
}