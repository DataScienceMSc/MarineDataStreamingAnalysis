package team;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class RendezVouzExecutor {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "./inputFiles/FarFromPorts.csv";

        SimpleConditionStreamGenerator generator = new SimpleConditionStreamGenerator();

        //Parsing input stream, indexing by mmsi
        DataStream<DynamicShipClass> parsedStream = generator.init(path, env);

        //generating a stream of InstuntaneousTurn events
        DataStream<SimpleEvent> GapStream= generator.generateStream(parsedStream,streamType.Gap);

    }

}
