package team;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;

public class IllegalShipping {


    public static void main(String[] args) throws Exception {
        //need to call somehow the gap simple event
        //need to also check for natura areas here

        //illegal fishing:
        //gap event+ isnide (or near) natura area
        //or
        //low speed + isnide (or near) nautra aera area

        //source: page 3 from paper
        //How not to drown in a sea of information: An event recognition approach


        //we do not want to do the whole parsing here:
        //So, geting the "FarFromPorts" + natura and finding the ships,
        //then finding gap and find the CEP is not OK.
        //we need to somehow just use event objects here and not create them again!

        //An Idea could be to create a wrapper object for each event class, which takes the stream as an input and
        //returns all the simple events (etc Low Speed), it finds in the stream given.

        //Then we use the simple events to create our complex event


        //example architecture


        //Need new wrapper classes for all our simple events:

        //(example wrapper class),
        //class LowSpeedEvents{
        // list lowSpeedEvents <----- list of simple low speed events
        // LowSpeedEvents(stream)   <---constructor reads stream and returns all the low speed events
        //


        //Complex Event Recognition Implementation example for IllegalFishing case :)

        // 1) Get the stream (from Kafka or just by reading the csv)
        // 2) call the constructor of wrapper object for low speed: This could look something like this
        // List lowSpeedEvents= LowSpeedEvents(stream)
        // 3) call the constructor of wrapper object for natura: This could look something like this
        // 4) List shipsInNatura= ShipsInNaturaEvents(stream)
        // 5)Union the two lists, so as to create a list of both lowSpeed and ShipsInNatura events
        // 6) create the pattern for the illegal fishing as usual

    }


}