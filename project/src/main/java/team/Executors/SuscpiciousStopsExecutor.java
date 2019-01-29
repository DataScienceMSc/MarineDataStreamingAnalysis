package team.Executors;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import team.ComplexEvents.*;
import team.General.DynamicShipClass;
import team.SimpleEvents.SimpleEvent;

public class SuscpiciousStopsExecutor {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);


        String path="./results/FarFromPorts.csv";

        TextInputFormat format = new TextInputFormat(new org.apache.flink.core.fs.Path(path));
        DataStream<java.lang.String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream= inputStream.map(line -> DynamicShipClass.fromString(line)).keyBy(element -> element.getmmsi());
        //generating a stream of StoppedAfterMoving events
        DataStream<SimpleEvent> StoppedAfterMovingStream= SimpleConditionStreamGenerator.generateStream(parsedStream,streamType.StoppedAfterMoving);

        //generating a stream of Gap events
        DataStream<SimpleEvent> GapStream= SimpleConditionStreamGenerator.generateStream(parsedStream,streamType.Gap);

        //concatenating the two streams
        DataStream<SimpleEvent> connectedStreams = StoppedAfterMovingStream.union(GapStream)
                .keyBy(element -> element.getMmsi());

        //Generating a complex event from the above (stop and then turn)
        SuspiciousStopsCE CE= new SuspiciousStopsCE(connectedStreams, "./results/suspiciousStops.csv",env);

        Thread t= new Thread(CE);
        t.start();

        //It compiles :)
        System.out.print("Hooooray");


    }
}
