package team.Executors;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import team.ComplexEvents.IllegalFishingCE_v1;

import team.General.DynamicShipClass;
import team.SimpleEvents.SimpleEvent;

public class IllegalFishingExecutor_V1 {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path="./results/FarFromPorts.csv";

        TextInputFormat format = new TextInputFormat(new org.apache.flink.core.fs.Path(path));
        DataStream<java.lang.String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream= inputStream.map(line -> DynamicShipClass.fromString(line)).keyBy(element -> element.getmmsi());


        //Generating the streams
        DataStream<SimpleEvent> naturaStream= SimpleConditionStreamGenerator.generateStream(parsedStream,streamType.Natura);
        DataStream<SimpleEvent> instantaneousTurnStream= SimpleConditionStreamGenerator.generateStream(parsedStream,streamType.InstantaneousTurn);


        //concatenating the streams
        DataStream<SimpleEvent> connectedStreams = instantaneousTurnStream.union(naturaStream)
                .keyBy(element -> element.getMmsi());



        //Generating a complex event from the above (stop and then turn)
        IllegalFishingCE_v1 CE1= new IllegalFishingCE_v1(connectedStreams, "./results/IllegalFishing-Natura&Turn.csv",env);

        Thread t1= new Thread(CE1);
        t1.start();


        //It compiles :)
        System.out.print("Hooooray");

    }
}
