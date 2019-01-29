package team.Executors;


import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import team.General.DynamicShipClass;
import team.SimpleEvents.*;

import java.util.Properties;


enum streamType{
    InstantaneousTurn,
    Stopped,
    LowSpeed,
    Gap,
    StoppedAfterMoving,
    Natura
} ;


public class SimpleConditionStreamGenerator {



    public static DataStream<String> getKafkaStream(StreamExecutionEnvironment env)
    {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "group21");
        // create Kafka's consumer
        FlinkKafkaConsumer09<String> myConsumer = new FlinkKafkaConsumer09<>("FinalOpenSeaEntries2", new SimpleStringSchema(), properties);
        // create the data stream
        DataStream<String> inputStream = env.addSource(myConsumer);

        return inputStream;
    }

    public static DataStream<SimpleEvent> generateStream(DataStream<DynamicShipClass> stream ,streamType type){

        String errorMsg= "Exception while generating simple event stream of:";

        DataStream <SimpleEvent> SimpleEventStream=null;

        switch (type){
            case Stopped:
                Stopped stopped = new Stopped();
                try {
                    SimpleEventStream=stopped.generateSimpleEvents(stream);
                }
                catch (Exception e) {
                    System.out.print(errorMsg + "Stopped Events");
                }

                break;
            case LowSpeed:
                LowSpeed lowSpeed = new LowSpeed();
                try {
                    SimpleEventStream=lowSpeed.generateSimpleEvents(stream);
                }
                catch (Exception e) {
                    System.out.print(errorMsg + "Low Speed Events");
                }
                break;

            case InstantaneousTurn:
                InstantaneousTurn instTurn = new InstantaneousTurn();
                try {
                    SimpleEventStream=instTurn.generateSimpleEvents(stream);
                }
                catch (Exception e) {
                    System.out.print(errorMsg + "Instantaneous Turn Events");
                }
                break;

            case Gap:
                Gaps gap = new Gaps();
                try {
                    SimpleEventStream=gap.generateSimpleEvents(stream);
                }
                catch (Exception e) {
                    System.out.print(errorMsg + "Gap Events");
                }
                break;
            case StoppedAfterMoving:
                StoppedAfterMoving sam = new StoppedAfterMoving();
                try {
                    SimpleEventStream=sam.generateSimpleEvents(stream);
                }
                catch (Exception e) {
                    System.out.print(errorMsg + "Stopped-After-Moving Events");
                }
                break;
            case Natura:
                NaturaAreas naturaAreas = new NaturaAreas();
                try {
                    SimpleEventStream=naturaAreas.generateSimpleEvents(stream);
                }
                catch (Exception e) {
                    System.out.print(errorMsg + "Natura Events");
                }
                break;

            default:
                 System.out.println("Unable to generate stream");
                 System.exit(1);
        }

        return SimpleEventStream;
    }

}
