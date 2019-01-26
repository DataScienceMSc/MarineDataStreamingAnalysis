package team.SimpleEvents;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.util.Collector;
import team.General.DynamicShipClass;
import team.General.GeoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NaturaAreas {


    public NaturaAreas(){}


    public static DataStream<SimpleEvent> generateSimpleEvents(DataStream<DynamicShipClass> parsedStream) throws Exception {

        GeoUtils geo = new GeoUtils();
        ArrayList<Integer> naturaArea = geo.latlonToGrid("./inputFiles/NaturaCentroidsFrance.csv");

        Pattern<DynamicShipClass, DynamicShipClass> naturaAreas = Pattern.<DynamicShipClass>begin("Natura", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new SimpleCondition<DynamicShipClass>() {

                    @Override
                    public boolean filter(DynamicShipClass value) throws Exception {
                        return naturaArea.contains(value.getGridId());
                    }
                }).oneOrMore();


            DataStream<SimpleEvent> natura = CEP.pattern(parsedStream, naturaAreas).
                    select((Map<String, List<DynamicShipClass>> pattern) -> {
                        System.out.println("Match Found!");
                        long startTime=pattern.get("Natura").get(0).getTs();
                        long endTime=pattern.get("Natura").get(0).getTs();
                        double lat = pattern.get("Natura").get(0).getLat();
                        double lon = pattern.get("Natura").get(0).getLon();
                        DynamicShipClass temp=pattern.get("Natura").get(0);
                        System.out.println("StartTime: "+startTime);
                        System.out.println("EndTime: "+endTime);
                        System.out.println("Duration: "+(endTime-startTime));


                        return new NaturaEvent(temp.getmmsi(),startTime,endTime,temp.getGridId(),lat, lon);
                    });
            return natura;
    }
}





