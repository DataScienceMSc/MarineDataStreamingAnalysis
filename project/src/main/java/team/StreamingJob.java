package team;
import java.io.PrintWriter;
import java.io.File;
import javafx.scene.control.Alert;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternFlatSelectFunction;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
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
import org.apache.flink.util.Collector;

import java.io.PrintStream;
import java.util.ArrayList;
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

		List<DynamicShipClass> sampleData = new ArrayList<>();
		sampleData.add(new DynamicShipClass(1, 1,1,0.0,1.0,1,1.0,1.0,1313));
		sampleData.add(new DynamicShipClass(1, 1,1,1.0,1.0,1,1.0,1.0,1312));
		sampleData.add(new DynamicShipClass(1, 1,1,1.5,1.0,1,1.0,1.0,1312));
		sampleData.add(new DynamicShipClass(1, 1,1,0.6,1.0,1,1.0,1.0,1312));
		sampleData.add(new DynamicShipClass(1, 1,1,1.7,1.0,1,1.0,1.0,1312));
		sampleData.add(new DynamicShipClass(1, 1,1,0.0,1.0,1,1.0,1.0,1313));


		sampleData.add(new DynamicShipClass(2, 1,1,0.0,1.0,1,1.0,1.0,1312));
		sampleData.add(new DynamicShipClass(2, 1,1,0.6,1.0,1,1.0,1.0,1312));

		sampleData.add(new DynamicShipClass(3, 1,1,1.0,1.0,1,1.0,1.0,1312));
		sampleData.add(new DynamicShipClass(4, 1,1,1.0,1.0,1,1.0,1.0,1312));
		sampleData.add(new DynamicShipClass(5, 1,1,1.0,1.0,1,1.0,1.0,1312));


		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1);

		DataStream<DynamicShipClass> keyedInput = env.fromCollection(sampleData).keyBy(element -> element.getmmsi());

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


		CEP.pattern(keyedInput, movingShip).flatSelect(new PatternFlatSelectFunction<DynamicShipClass, String>() {
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
		}).writeAsText("/home/gelou/Desktop/test.txt", FileSystem.WriteMode.OVERWRITE);

		env.execute();


	}


}
