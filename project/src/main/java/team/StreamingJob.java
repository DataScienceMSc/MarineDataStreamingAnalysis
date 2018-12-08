/*
package team;

import javafx.scene.control.Alert;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.flink.util.Collector;
import scala.Proxy;


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
		FlinkKafkaConsumer09<DynamicShipClass> myConsumer = new FlinkKafkaConsumer09<>("test", new DynamicShipSchema(), properties);

		// create the data stream
		DataStream<DynamicShipClass> ships = env.addSource(myConsumer)
				.assignTimestampsAndWatermarks(new AssignWatermarks())
				.keyBy(new KeySelector<DynamicShipClass, Integer>() {
			@Override
			public Integer getKey(DynamicShipClass value) throws Exception {
				return value.getmmsi();
			}});


		Pattern<DynamicShipClass, DynamicShipClass> movingShip = Pattern.<DynamicShipClass>begin("stoppedBefore")
				.where(new SimpleCondition<DynamicShipClass>() {
					//private static final long serialVersionUID = 314415972814127035L;

					@Override
					public boolean filter(DynamicShipClass value) throws Exception {
						return value.getSpeed()==0.0;
					}
				})
				.followedBy("middle")
				.where(new IterativeCondition<DynamicShipClass>() {
					//private static final long serialVersionUID = 6664468385615273240L;

					@Override
					public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
						return value.getSpeed() > 0.0;
					}
				}).times(2)
				.followedBy("end").where(new IterativeCondition<DynamicShipClass>() {
					//private static final long serialVersionUID = 6664468385615273240L;

					@Override
					public boolean filter(DynamicShipClass value, Context<DynamicShipClass> ctx) throws Exception {
						return value.getSpeed() == 0.0;
					}
				});


		CEP.pattern(ships, movingShip).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {
			private static final long serialVersionUID = -8972838879934875538L;

			@Override
			public void flatSelect(Map<String, List<DynamicShipClass>> map, Collector<String> collector) throws Exception {
				StringBuilder str = new StringBuilder();
				System.out.println("here");
				for (Map.Entry<String, List<DynamicShipClass>> entry: map.entrySet()) {
					for (DynamicShipClass t: entry.getValue()) {
						str.append(t.getmmsi());
						str.append("            ");
						str.append(t.getSpeed());
					}
				}
				collector.collect(str.toString());
			}
		}).writeAsText("/home/eleni/Documents/output.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

		*/
/*DataStream<alert> outputStream = patternStream.select(new PatternSelectFunction<DynamicShipClass, alert>() {
			@Override
			public alert select(Map<String, List<DynamicShipClass>> pattern) throws Exception {
				DynamicShipClass first = (DynamicShipClass) pattern.get("stoppedBefore");
				DynamicShipClass second = (DynamicShipClass) pattern.get("moving");
				return new alert(first.getmmsi(), second.getSpeed());
			}

		});*//*




		//outputStream.map(v -> v.toString()).writeAsText("/home/eleni/Documents/output.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);


		env.execute("StreamingJob");


	}


}*/
