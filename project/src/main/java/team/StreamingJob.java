/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package team;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;

import java.util.Properties;



/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
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
		// create the data stream
		DataStream<String> stream = env.addSource(myConsumer);
//		DataStream<Tuple9<Integer, Integer, Integer, Double, Double, Integer, Double, Double, Integer>> parsedata = stream
//				.map((line) -> {
//					String[] cells = line.split(",");
//					// Only keep first and third cells
//					return new Tuple9<Integer, Integer, Integer, Double, Double, Integer, Double, Double, Integer>(Integer.parseInt(cells[0]), Integer.parseInt(cells[1]), Integer.parseInt(cells[2]), Double.parseDouble(cells[3]),
//							Double.parseDouble(cells[4]), Integer.parseInt(cells[5]), Double.parseDouble(cells[6]), Double.parseDouble(cells[7]),
//							Integer.parseInt(cells[8]));
//				}).returns(new TypeHint<Tuple9<Integer, Integer, Integer, Double, Double, Integer, Double, Double, Integer>>() {
//					@Override
//					public TypeInformation<Tuple9<Integer, Integer, Integer, Double, Double, Integer, Double, Double, Integer>> getTypeInfo() {
//						return super.getTypeInfo();
//					}
//				}).keyBy("id");
		DataStream<DynamicShipClass> ships = stream.map(line -> DynamicShipClass.fromString(line)).keyBy("mmsi");
		// print() will write the contents of the stream to the TaskManager's standard out stream
		ships.print();

		env.execute();

	}
}
