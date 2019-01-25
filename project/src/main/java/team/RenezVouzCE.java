package team;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.util.Collector;
import java.util.List;
import java.util.Map;


public class RendezVouz {

    RendezVouz() {
    }

    ;

    static void GenerateComplexEvents(DataStream<SimpleEvent> parsedStream, String outputFile) throws Exception {

//        Pattern<SimpleEvent, ?> complex = Pattern.<SimpleEvent>begin("start")
//............


//        CEP.pattern(parsedStream, complex).flatSelect(new PatternFlatSelectFunction<SimpleEvent, String>() {
//......

    }
}
