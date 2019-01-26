package team.Executors;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import team.ComplexEvents.*;
import team.General.DynamicShipClass;
import team.SimpleEvents.SimpleEvent;

public class SuscpiciousStopsExecutor {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "./inputFiles/FarFromPorts.csv";

        SimpleConditionStreamGenerator generator = new SimpleConditionStreamGenerator();

        //Parsing input stream, indexing by mmsi
        DataStream<DynamicShipClass> parsedStream = generator.init(path, env);

        //generating a stream of StoppedAfterMoving events
        DataStream<SimpleEvent> StoppedAfterMovingStream= generator.generateStream(parsedStream,streamType.StoppedAfterMoving);

        //generating a stream of Gap events
        DataStream<SimpleEvent> GapStream= generator.generateStream(parsedStream,streamType.Gap);

        //concatenating the two streams
        DataStream<SimpleEvent> connectedStreams = StoppedAfterMovingStream.union(GapStream)
                .keyBy(element -> element.getMmsi());

        //Generating a complex event from the above (stop and then turn)
        SuspiciousStopsCE CE= new SuspiciousStopsCE();
        CE.GenerateComplexEvents(connectedStreams, "./results/suspiciousStops.csv");

        //It compiles :)
        System.out.print("Hooooray");

        env.execute();


    }
}
