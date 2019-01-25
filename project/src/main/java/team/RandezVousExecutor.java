package team;


import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class RandezVousExecutor {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "./inputFiles/FarFromPorts.csv";

        SimpleConditionStreamGenerator generator= new SimpleConditionStreamGenerator();

        //Parsing input stream, indexing by mmsi
        DataStream<DynamicShipClass> parsedStream=generator.init(path, env);


        //generating a stream of Stopped events
        DataStream<SimpleEvent> StoppedStream= generator.generateStream(parsedStream,streamType.Stopped);

        //generating a stream of Gap Events
        DataStream<SimpleEvent> GapStream= generator.generateStream(parsedStream,streamType.Stopped);

        //concatenating the two streams
        DataStream<SimpleEvent> connectedStreams = GapStream.union(StoppedStream)
                .keyBy(element -> element.getGridId());


        //Generating a complex event from the above (stop and then turn)
        RandezVousCE CE= new RandezVousCE();
        CE.GenerateComplexEvents(connectedStreams, "./results/RandezVous.csv");

        //It compiles :)
        System.out.print("Hooooray");

        env.execute();
    }


}