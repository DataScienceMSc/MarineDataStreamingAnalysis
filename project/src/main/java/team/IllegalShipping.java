package team;


import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class IllegalShipping {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "./inputFiles/FarFromPorts_small.csv";


        SimpleConditionStreamGenerator generator= new SimpleConditionStreamGenerator();

        //Parsing input stream, indexing by mmsi
        DataStream<DynamicShipClass> parsedStream=generator.init(path);

        //generating a stream of InstuntaneousTurn events
        DataStream<SimpleEvent> InstantaneousTurnStream= generator.generateStream(parsedStream,streamType.InstantaneousTurn);

        //It compiles :)
        System.out.print("Hooooray");
    }


}