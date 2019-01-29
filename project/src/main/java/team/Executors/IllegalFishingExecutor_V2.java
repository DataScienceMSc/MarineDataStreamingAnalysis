package team.Executors;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import team.ComplexEvents.IllegalFishingCE_v2;
import team.General.DynamicShipClass;
import team.SimpleEvents.SimpleEvent;

public class IllegalFishingExecutor_V2 {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);


        String path="./results/FarFromPorts.csv";

        TextInputFormat format = new TextInputFormat(new org.apache.flink.core.fs.Path(path));
        DataStream<String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        DataStream<DynamicShipClass> parsedStream= inputStream.map(line -> DynamicShipClass.fromString(line)).keyBy(element -> element.getmmsi());


        //Generating the streams
        DataStream<SimpleEvent> naturaStream= SimpleConditionStreamGenerator.generateStream(parsedStream,streamType.Natura);
        DataStream<SimpleEvent> lowSpeed= SimpleConditionStreamGenerator.generateStream(parsedStream,streamType.LowSpeed);


        //concatenating the streams
        DataStream<SimpleEvent> connectedStreams = lowSpeed.union(naturaStream)
                .keyBy(element -> element.getMmsi());


       //Generating a complex event from the above (stop and then turn)
        IllegalFishingCE_v2 CE2= new IllegalFishingCE_v2(connectedStreams, "./results/IllegalFishing-Natura&LowSpeed.csv",env);

        Thread t2= new Thread(CE2);
        t2.start();


        //It compiles :)
        System.out.print("Hooooray");

    }
}
