package team.Executors;


import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import team.ComplexEvents.StopAndTurnCE;
import team.General.DynamicShipClass;
import team.SimpleEvents.SimpleEvent;


public class StopAndTurnExecutor {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);


        String path="./results/FarFromPorts.csv";

        TextInputFormat format = new TextInputFormat(new org.apache.flink.core.fs.Path(path));
        DataStream<java.lang.String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream= inputStream.map(line -> DynamicShipClass.fromString(line)).keyBy(element -> element.getmmsi());

        //generating a stream of InstuntaneousTurn events
        DataStream<SimpleEvent> InstantaneousTurnStream= SimpleConditionStreamGenerator.generateStream(parsedStream,streamType.InstantaneousTurn);

        //generating a stream of Stopped events
        DataStream<SimpleEvent> StoppedStream= SimpleConditionStreamGenerator.generateStream(parsedStream,streamType.Stopped);

        //concatenating the two streams
        DataStream<SimpleEvent> connectedStreams = StoppedStream.union(InstantaneousTurnStream)
                .keyBy(element -> element.getMmsi());


        //Generating a complex event from the above (stop and then turn)
        StopAndTurnCE CE= new StopAndTurnCE(connectedStreams, "./results/StopAndTurn.csv",env);

        Thread t= new Thread(CE);
        t.start();

        //It compiles :)
        System.out.print("Hooooray");

    }
}