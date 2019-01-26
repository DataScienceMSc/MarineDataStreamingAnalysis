package team.Executors;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import team.General.DynamicShipClass;
import team.SimpleEvents.*;


public class SimplePatternExecutor {


    public static void main(String[] args) throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "./inputFiles/FarFromPorts.csv";

        SimpleConditionStreamGenerator generator = new SimpleConditionStreamGenerator();

        //Parsing input stream, indexing by mmsi
        DataStream<DynamicShipClass> parsedStream = generator.init(path, env);


       /* Acceleration acceleration = new Acceleration();
        acceleration.outputSimpleEvents(parsedStream, "./results/Acceleration.txt");

        FarFromPorts farFromPorts = new FarFromPorts();
        farFromPorts.outputSimpleEvents(parsedStream, "./results/FarFromPorts.txt");

        SpeedChange speedChange = new SpeedChange();
        speedChange.outputSimpleEvents(parsedStream, "./results/speedChange.txt");

        SlowMotion slowMotion = new SlowMotion();
        slowMotion.outputSimpleEvents(parsedStream, "./results/slowMotion.txt");

        IncreasingSpeed increasingSpeed = new IncreasingSpeed();
        increasingSpeed.outputSimpleEvents(parsedStream, "./results/increasingSpeed.txt");


        UnderWay underWay = new UnderWay();
        underWay.outputSimpleEvents(parsedStream, "./results/underWay.txt");*/


        RendezVousSimple randezVous = new RendezVousSimple();
        randezVous.outputSimpleEvents(parsedStream,"./results/randezVous.txt");

        //It compiles :)
        System.out.print("Hooooray");

        env.execute();
        }
}