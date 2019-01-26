package team.Executors;


import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import team.ComplexEvents.StopAndTurnCE;
import team.General.DynamicShipClass;
import team.SimpleEvents.SimpleEvent;


public class StopAndTurnExecutor {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "./inputFiles/FarFromPorts.csv";

        SimpleConditionStreamGenerator generator= new SimpleConditionStreamGenerator();

        //Parsing input stream, indexing by mmsi
        DataStream<DynamicShipClass> parsedStream=generator.init(path, env);

        //generating a stream of InstuntaneousTurn events
        DataStream<SimpleEvent> InstantaneousTurnStream= generator.generateStream(parsedStream,streamType.InstantaneousTurn);

        //generating a stream of Stopped events
        DataStream<SimpleEvent> StoppedStream= generator.generateStream(parsedStream,streamType.Stopped);

        //concatenating the two streams
        DataStream<SimpleEvent> connectedStreams = StoppedStream.union(InstantaneousTurnStream)
                .keyBy(element -> element.getMmsi());


        //Generating a complex event from the above (stop and then turn)
        StopAndTurnCE CE= new StopAndTurnCE();
        CE.GenerateComplexEvents(connectedStreams, "./results/stopAndTurn.csv");

        //It compiles :)
        System.out.print("Hooooray");

        env.execute();
    }
}