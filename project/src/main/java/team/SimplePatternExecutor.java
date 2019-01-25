package team;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class SimplePatternExecutor {


    public static void main(String[] args) throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "./inputFiles/FarFromPorts.csv";

        SimpleConditionStreamGenerator generator = new SimpleConditionStreamGenerator();

        //Parsing input stream, indexing by mmsi
        DataStream<DynamicShipClass> parsedStream = generator.init(path, env);


        Acceleration acc = new Acceleration();
        acc.outputSimpleEvents(parsedStream, "./results/Acceleration.txt");

        FarFromPorts ffp = new FarFromPorts();
        ffp.outputSimpleEvents(parsedStream, "./results/FarFromPorts.txt");


        //It compiles :)
        System.out.print("Hooooray");

        env.execute();
        }
}