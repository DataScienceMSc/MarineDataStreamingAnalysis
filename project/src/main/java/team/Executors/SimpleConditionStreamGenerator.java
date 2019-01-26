package team.Executors;


import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import team.General.DynamicShipClass;
import team.SimpleEvents.*;


enum streamType{
    InstantaneousTurn,
    Stopped,
    LowSpeed,
    Gap,
    StoppedAfterMoving,
    Natura
} ;


public class SimpleConditionStreamGenerator {

    SimpleConditionStreamGenerator(){};


    DataStream<DynamicShipClass> init(java.lang.String path, StreamExecutionEnvironment env) throws Exception{

        TextInputFormat format = new TextInputFormat(new org.apache.flink.core.fs.Path(path));
        DataStream<java.lang.String> inputStream = env.readFile(format, path, FileProcessingMode.PROCESS_CONTINUOUSLY, 100);

        return inputStream.map(line -> DynamicShipClass.fromString(line)).keyBy(element -> element.getmmsi());   //to run randezVous pattern change to element.getGridId()

    }


    DataStream<SimpleEvent> generateStream(DataStream<DynamicShipClass> stream ,streamType type){

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
