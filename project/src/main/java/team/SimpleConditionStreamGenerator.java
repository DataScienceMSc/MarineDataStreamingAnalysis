package team;


import org.apache.commons.lang3.ObjectUtils;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;

enum streamType{
    SpeedChange,
    InstantaneousTurn,
    Stopped,
    LowSpeed
} ;


public class SimpleConditionStreamGenerator {

    SimpleConditionStreamGenerator(){};


    DataStream<DynamicShipClass> init(java.lang.String path){

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        TextInputFormat format = new TextInputFormat(new org.apache.flink.core.fs.Path(path));
        DataStream<java.lang.String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        return inputStream.map(line -> DynamicShipClass.fromString(line)).keyBy(element -> element.getmmsi());

    }


    DataStream<SimpleEvent> generateStream(DataStream<DynamicShipClass> stream ,streamType type){

        String errorMsg= "Exception while generating simple event stream of:";

        DataStream <SimpleEvent> SimpleEventStream=null;

        switch (type){
            case Stopped:
                break;
            case LowSpeed:
                break;
            case SpeedChange:
                break;
            case InstantaneousTurn:
                InstantaneousTurn simpleEvent = new InstantaneousTurn();
                try {
                    SimpleEventStream=simpleEvent.generateSimpleEvents(stream);
                }
                catch (Exception e) {
                    System.out.print(errorMsg + "Instantaneous Turn Events");
                }
                break;
             default:
                 System.out.println("Unable to generate stream");
                 System.exit(1);
        }
        return SimpleEventStream;
    }

}
