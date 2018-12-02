package team;

import javafx.scene.control.Alert;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.api.common.serialization.Encoder;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;


 	//* Skeleton for a Flink Streaming Job.
  	//* <p>For a tutorial how to write a Flink streaming application, check the
 	//* tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
 	//*
 	//* <p>To package your application into a JAR file for execution, run
 	//* 'mvn clean package' on the command line.
 	//*
 	//* <p>If you change the name of the main class (with the public static void main(String[] args))
 	//* method, change the respective entry in the POM.xml file (simply search for 'mainClass').


public class StreamingJob {

	public static void main(String[] args) throws Exception {

		// create execution environment
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		Properties properties = new Properties();
		properties.setProperty("bootstrap.servers", "localhost:9092");
		properties.setProperty("group.id", "test");

		// create Kafka's consumer
		FlinkKafkaConsumer09<String> myConsumer = new FlinkKafkaConsumer09<>("test", new SimpleStringSchema(), properties);

		// create the data stream
		DataStream<DynamicShipClass> ships = env.addSource(myConsumer).map(line -> DynamicShipClass.fromString(line));
		ships.keyBy("mmsi").assignTimestampsAndWatermarks(new AscendingTimestampExtractor<DynamicShipClass>() {

			@Override
			public long extractAscendingTimestamp(DynamicShipClass ts) {
				return ts.getEventTime();
			}
		});


		Pattern<DynamicShipClass, ?> movingShip =
				Pattern.<DynamicShipClass>begin("stoppedBefore")
						.where(new SimpleCondition<DynamicShipClass>() {
							@Override
							public boolean filter(DynamicShipClass event) throws Exception {
								return event.speed == 0.0;
							}
						})
						.next("moving")
						.where(new SimpleCondition<DynamicShipClass>() {
							@Override
							public boolean filter(DynamicShipClass event) throws Exception {
								return event.speed != 0.0;
							}
						});
						/*.followedBy("stoppedAfter")
						.where(new SimpleCondition<DynamicShipClass>() {
							@Override
							public boolean filter(DynamicShipClass event) throws Exception {
								return event.speed == 0;
							}
						});*/


		PatternStream<DynamicShipClass> patternStream = CEP.pattern(ships, movingShip);
		DataStream<String> outputStream = patternStream.select(new PatternSelectFunction<DynamicShipClass, String>() {
			@Override
			public String select(Map<String, List<DynamicShipClass>> pattern) throws Exception {
				DynamicShipClass first = pattern.get("stoppedBefore").get(0);
				DynamicShipClass second = pattern.get("moving").get(0);
				if(first.getmmsi() == second.getmmsi()){
					return "Shipment : " + first.getSpeed() + second.getSpeed() + " was dropped!";
				}else{
					return "ok";
				}

			}
		});

		outputStream.map(v -> v.toString()).writeAsText("/Users/thanasiskaridis/Desktop/maritime/MarineDataStreamingAnalysis/project/output.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);



		/**/
		// print() will write
		// the contents of the stream to the TaskManager's standard out stream
		//patternStream.print();

		env.execute();


	}


}
