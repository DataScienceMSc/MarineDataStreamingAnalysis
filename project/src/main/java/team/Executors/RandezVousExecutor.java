package team.Executors;


import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import team.ComplexEvents.*;
import team.General.DynamicShipClass;
import team.SimpleEvents.SimpleEvent;

public class RandezVousExecutor {


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);


        String path="./results/FarFromPorts.csv";

        TextInputFormat format = new TextInputFormat(new org.apache.flink.core.fs.Path(path));
        DataStream<java.lang.String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream= inputStream.map(line -> DynamicShipClass.fromString(line)).
                keyBy(element -> element.getGridId());
        //to run randezVous pattern change to element.getGridId()


        //generating a stream of Stopped events
        DataStream<SimpleEvent> StoppedStream= SimpleConditionStreamGenerator.generateStream(parsedStream,streamType.Stopped);

        //generating a stream of Gap Events
        DataStream<SimpleEvent> GapStream= SimpleConditionStreamGenerator.generateStream(parsedStream,streamType.Gap);

        //concatenating the two streams
        DataStream<SimpleEvent> connectedStreams = GapStream.union(StoppedStream)
                .keyBy(element -> element.getGridId());


        //Generating a complex event from the above (stop and then turn)
        RandezVousCE CE= new RandezVousCE(connectedStreams, "./results/RandezVous.csv",env);

        Thread t= new Thread(CE);
        t.start();

        System.out.print("Hooooray");

    }


}