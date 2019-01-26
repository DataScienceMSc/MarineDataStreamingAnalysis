package team.Executors;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import team.ComplexEvents.IllegalFishingCE_v1;
import team.ComplexEvents.IllegalFishingCE_v2;
import team.ComplexEvents.IllegalFishingCE_v3;
import team.General.DynamicShipClass;
import team.SimpleEvents.SimpleEvent;

public class IllegalFishingExecutor {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        String path = "./inputFiles/FarFromPorts.csv";

        SimpleConditionStreamGenerator generator = new SimpleConditionStreamGenerator();

        //Parsing input stream, indexing by mmsi
        DataStream<DynamicShipClass> parsedStream = generator.init(path, env);


        //Generating the streams
        DataStream<SimpleEvent> naturaStream= generator.generateStream(parsedStream,streamType.Natura);
        DataStream<SimpleEvent> instantaneousTurnStream= generator.generateStream(parsedStream,streamType.InstantaneousTurn);
        DataStream<SimpleEvent> lowSpeed= generator.generateStream(parsedStream,streamType.LowSpeed);
        DataStream<SimpleEvent> gap= generator.generateStream(parsedStream,streamType.Gap);


        //concatenating the streams
        DataStream<SimpleEvent> connectedStreams = instantaneousTurnStream.union(naturaStream,lowSpeed,gap)
                .keyBy(element -> element.getMmsi());


        //3 versions of Illegal events are shown below


        //Version 1: Illegal Fishing=Natura + Instantaneous Turn
        IllegalFishingCE_v1 CE= new IllegalFishingCE_v1();
        CE.GenerateComplexEvents(connectedStreams, "./results/IllegalFishing-Natura&Turn.csv");


        //Version 2: Illegal Fishing=Natura + Low Speed
        IllegalFishingCE_v2 CE2= new IllegalFishingCE_v2();
        CE.GenerateComplexEvents(connectedStreams, "./results/IllegalFishing-Natura&LowSpeed.csv");


        //Version 3: Illegal Fishing=Natura + Gap
        IllegalFishingCE_v3 CE3= new IllegalFishingCE_v3();

        //It compiles :)
        System.out.print("Hooooray");

        env.execute();

    }
}
